package com.streamverse.video.service;

import com.streamverse.video.dto.request.UploadUrlRequest;
import com.streamverse.video.dto.request.VideoMetadataRequest;
import com.streamverse.video.dto.request.VideoUpdateRequest;
import com.streamverse.video.dto.response.UploadUrlResponse;
import com.streamverse.video.dto.response.VideoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VideoService {

    /** STEP 1: issue a presigned URL for React to upload one file directly to R2. */
    UploadUrlResponse createUploadUrl(UploadUrlRequest request);

    /** STEP 3: persist video metadata after the file is already in R2. */
    VideoResponse saveMetadata(VideoMetadataRequest request);

    VideoResponse getById(UUID id);

    Page<VideoResponse> getAll(Pageable pageable);

    VideoResponse update(UUID id, VideoUpdateRequest request);

    /** Soft-delete: sets is_deleted = true. Never removes the row. */
    void softDelete(UUID id);
}
