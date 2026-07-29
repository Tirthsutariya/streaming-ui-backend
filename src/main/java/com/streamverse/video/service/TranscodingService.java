package com.streamverse.video.service;

import java.util.UUID;

public interface TranscodingService {

    /**
     * Downloads the source from R2, generates an HLS adaptive ladder with
     * FFmpeg, uploads every playlist + segment back to R2, and updates the
     * video row's status and hlsUrl. Runs asynchronously.
     *
     * @param videoId   the video row to update
     * @param sourceKey the R2 object key of the uploaded original file
     */
    void transcodeToHls(UUID videoId, String sourceKey);
}
