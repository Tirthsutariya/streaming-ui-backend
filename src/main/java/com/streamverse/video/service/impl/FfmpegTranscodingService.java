package com.streamverse.video.service.impl;

import com.streamverse.video.config.FfmpegProperties;
import com.streamverse.video.constant.VideoStatus;
import com.streamverse.video.entity.Video;
import com.streamverse.video.repository.VideoRepository;
import com.streamverse.video.service.StorageService;
import com.streamverse.video.service.TranscodingService;
import com.streamverse.video.transcode.Rendition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FfmpegTranscodingService implements TranscodingService {

    private static final String HLS_PREFIX = "hls";
    private static final String MASTER = "master.m3u8";

    private final StorageService storageService;
    private final VideoRepository videoRepository;
    private final FfmpegProperties props;

    @Override
    @Async("transcodeExecutor")
    public void transcodeToHls(final UUID videoId, final String sourceKey) {
        final Path jobDir = Path.of(props.getWorkDir(), videoId.toString());
        try {
            Files.createDirectories(jobDir);

            // 1. Download the original from R2 to local disk.
            final Path source = jobDir.resolve("source" + extensionOf(sourceKey));
            downloadSource(sourceKey, source);

            // 2. Probe the source height so we never upscale.
            final int sourceHeight = probeHeight(source);
            final List<Rendition> ladder = Rendition.applicableFor(sourceHeight);
            log.info("Transcoding {} (source height {}) into {} renditions",
                    videoId, sourceHeight, ladder.size());

            // 3. Run FFmpeg -> master.m3u8 + stream_N/{playlist.m3u8, *.ts}
            runFfmpeg(source, jobDir, ladder);

            // 4. Upload every generated file to R2 under hls/{videoId}/...
            uploadHlsTree(jobDir, videoId);

            // 5. Mark READY with the master playlist URL.
            final String masterKey = HLS_PREFIX + "/" + videoId + "/" + MASTER;
            markReady(videoId, storageService.getPublicUrl(masterKey));
            log.info("Transcoding complete for {}", videoId);

        } catch (final Exception e) {
            log.error("Transcoding failed for {}: {}", videoId, e.getMessage(), e);
            markFailed(videoId);
        } finally {
            deleteQuietly(jobDir);
        }
    }

    // ---------- steps ----------

    private void downloadSource(final String sourceKey, final Path target) throws Exception {
        try (InputStream in = storageService.download(sourceKey)) {
            Files.copy(in, target);
        }
    }

    private int probeHeight(final Path source) throws Exception {
        final List<String> cmd = List.of(
                props.getProbePath(),
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=height",
                "-of", "csv=p=0",
                source.toString());
        final String out = runProcess(cmd, source.getParent()).trim();
        try {
            return Integer.parseInt(out.replaceAll("[^0-9].*$", ""));
        } catch (final NumberFormatException e) {
            throw new IllegalStateException("Could not read source height from ffprobe: '" + out + "'");
        }
    }

    private void runFfmpeg(final Path source, final Path jobDir, final List<Rendition> ladder)
            throws Exception {

        // Sub-folders for each variant playlist + its segments.
        for (int i = 0; i < ladder.size(); i++) {
            Files.createDirectories(jobDir.resolve("stream_" + i));
        }

        final List<String> cmd = new ArrayList<>();
        cmd.add(props.getPath());
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(source.toString());

        // Split the decoded video into N branches, one scaled per rendition.
        final StringBuilder filter = new StringBuilder();
        filter.append("[0:v]split=").append(ladder.size());
        for (int i = 0; i < ladder.size(); i++) {
            filter.append("[v").append(i).append("]");
        }
        filter.append(";");
        for (int i = 0; i < ladder.size(); i++) {
            final Rendition r = ladder.get(i);
            filter.append("[v").append(i).append("]scale=w=")
                    .append(r.width()).append(":h=").append(r.height())
                    .append(":force_original_aspect_ratio=decrease,")
                    .append("pad=").append(r.width()).append(":").append(r.height())
                    .append(":(ow-iw)/2:(oh-ih)/2[v").append(i).append("out]");
            if (i < ladder.size() - 1) {
                filter.append(";");
            }
        }
        cmd.add("-filter_complex");
        cmd.add(filter.toString());

        // Per-rendition video + audio encode settings.
        final StringBuilder varMap = new StringBuilder();
        for (int i = 0; i < ladder.size(); i++) {
            final Rendition r = ladder.get(i);
            cmd.add("-map");
            cmd.add("[v" + i + "out]");
            cmd.add("-c:v:" + i);
            cmd.add("libx264");
            cmd.add("-preset");
            cmd.add("veryfast");
            cmd.add("-b:v:" + i);
            cmd.add(r.videoBitrateKbps() + "k");
            cmd.add("-maxrate:v:" + i);
            cmd.add(r.maxrateKbps() + "k");
            cmd.add("-bufsize:v:" + i);
            cmd.add(r.bufsizeKbps() + "k");

            cmd.add("-map");
            cmd.add("a:0?");                 // optional: tolerate sources without audio
            cmd.add("-c:a:" + i);
            cmd.add("aac");
            cmd.add("-b:a:" + i);
            cmd.add(r.audioBitrateKbps() + "k");

            varMap.append("v:").append(i).append(",a:").append(i);
            if (i < ladder.size() - 1) {
                varMap.append(" ");
            }
        }

        // Keyframe cadence aligned to segment length for clean quality switches.
        cmd.add("-g");
        cmd.add("48");
        cmd.add("-keyint_min");
        cmd.add("48");
        cmd.add("-sc_threshold");
        cmd.add("0");

        // HLS output with a generated master playlist.
        cmd.add("-f");
        cmd.add("hls");
        cmd.add("-hls_time");
        cmd.add(String.valueOf(props.getHlsTimeSeconds()));
        cmd.add("-hls_playlist_type");
        cmd.add("vod");
        cmd.add("-hls_flags");
        cmd.add("independent_segments");
        cmd.add("-hls_segment_filename");
        cmd.add(jobDir.resolve("stream_%v").resolve("data_%03d.ts").toString());
        cmd.add("-master_pl_name");
        cmd.add(MASTER);
        cmd.add("-var_stream_map");
        cmd.add(varMap.toString());
        cmd.add(jobDir.resolve("stream_%v").resolve("playlist.m3u8").toString());

        runProcess(cmd, jobDir);
    }

    private void uploadHlsTree(final Path jobDir, final UUID videoId) throws Exception {
        try (Stream<Path> files = Files.walk(jobDir)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> {
                        final String n = p.getFileName().toString();
                        return n.endsWith(".m3u8") || n.endsWith(".ts");
                    })
                    .forEach(p -> {
                        final String rel = jobDir.relativize(p).toString().replace('\\', '/');
                        final String key = HLS_PREFIX + "/" + videoId + "/" + rel;
                        storageService.uploadFile(key, p, contentType(rel));
                    });
        }
    }

    // ---------- db updates (own transactions via repository) ----------

    private void markReady(final UUID videoId, final String hlsUrl) {
        videoRepository.findById(videoId).ifPresent(v -> {
            v.setHlsUrl(hlsUrl);
            v.setStatus(VideoStatus.READY);
            videoRepository.save(v);
        });
    }

    private void markFailed(final UUID videoId) {
        videoRepository.findById(videoId).ifPresent(v -> {
            v.setStatus(VideoStatus.FAILED);
            videoRepository.save(v);
        });
    }

    // ---------- process helper ----------

    /** Runs a process, streams its output to the log, returns stdout, throws on non-zero exit. */
    private String runProcess(final List<String> command, final Path workingDir) throws Exception {
        final ProcessBuilder pb = new ProcessBuilder(command)
                .redirectErrorStream(true);
        if (workingDir != null) {
            pb.directory(workingDir.toFile());
        }
        final Process process = pb.start();

        final StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        final int exit = process.waitFor();
        if (exit != 0) {
            throw new IllegalStateException(
                    "Command failed (exit " + exit + "): " + String.join(" ", command)
                            + System.lineSeparator() + output);
        }
        return output.toString();
    }

    // ---------- small utils ----------

    private String contentType(final String name) {
        if (name.endsWith(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        }
        if (name.endsWith(".ts")) {
            return "video/mp2t";
        }
        return "application/octet-stream";
    }

    private String extensionOf(final String key) {
        final int dot = key.lastIndexOf('.');
        return dot >= 0 ? key.substring(dot) : ".mp4";
    }

    private void deleteQuietly(final Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (final Exception ignored) {
                    // best-effort cleanup
                }
            });
        } catch (final Exception ignored) {
            // best-effort cleanup
        }
    }
}
