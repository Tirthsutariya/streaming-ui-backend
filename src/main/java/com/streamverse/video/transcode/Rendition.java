package com.streamverse.video.transcode;

import java.util.List;

/**
 * One quality level in the adaptive ladder. Bitrates are conservative H.264
 * ABR targets. We never upscale — a rendition is only produced if the source
 * is at least as tall as {@code height}.
 */
public record Rendition(
        String name,
        int width,
        int height,
        int videoBitrateKbps,
        int maxrateKbps,
        int bufsizeKbps,
        int audioBitrateKbps) {

    /** Full ladder, smallest first. */
    public static final List<Rendition> LADDER = List.of(
            new Rendition("240p", 426, 240, 400, 480, 600, 64),
            new Rendition("360p", 640, 360, 800, 960, 1200, 96),
            new Rendition("480p", 854, 480, 1400, 1680, 2100, 128),
            new Rendition("720p", 1280, 720, 2800, 3360, 4200, 128),
            new Rendition("1080p", 1920, 1080, 5000, 6000, 7500, 192)
    );

    /** Renditions that do not exceed the source height (never upscale). */
    public static List<Rendition> applicableFor(final int sourceHeight) {
        final List<Rendition> fits = LADDER.stream()
                .filter(r -> r.height() <= sourceHeight)
                .toList();
        // Always produce at least the smallest rendition.
        return fits.isEmpty() ? List.of(LADDER.get(0)) : fits;
    }
}
