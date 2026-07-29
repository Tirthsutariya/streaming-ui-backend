package com.streamverse.video.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ffmpeg")
public class FfmpegProperties {

    /** Path to the ffmpeg binary (on PATH by default). */
    private String path = "ffmpeg";

    /** Path to the ffprobe binary (on PATH by default). */
    private String probePath = "ffprobe";

    /** Local scratch directory for downloads + HLS output. */
    private String workDir = System.getProperty("java.io.tmpdir") + "/streamverse-transcode";

    /** HLS segment length in seconds. */
    private int hlsTimeSeconds = 6;
}
