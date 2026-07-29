package com.streamverse.video.constant;

/**
 * Lifecycle of a video's HLS transcoding pipeline.
 */
public enum VideoStatus {
    /** Row saved, transcoding not started yet. */
    PENDING,
    /** FFmpeg is generating HLS renditions. */
    PROCESSING,
    /** HLS renditions are in R2 and playable. */
    READY,
    /** Transcoding failed; only the original (progressive) file is available. */
    FAILED
}
