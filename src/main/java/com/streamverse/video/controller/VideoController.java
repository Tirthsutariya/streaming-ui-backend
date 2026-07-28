package com.streamverse.video.controller;

import com.streamverse.video.common.EntityResponse;
import com.streamverse.video.common.MetaData;
import com.streamverse.video.dto.request.UploadUrlRequest;
import com.streamverse.video.dto.request.VideoMetadataRequest;
import com.streamverse.video.dto.request.VideoUpdateRequest;
import com.streamverse.video.dto.response.UploadUrlResponse;
import com.streamverse.video.dto.response.VideoResponse;
import com.streamverse.video.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /** STEP 1: React requests a presigned URL to upload a file directly to R2. */
    @PostMapping("/upload-url")
    public ResponseEntity<EntityResponse> createUploadUrl(
            @Valid @RequestBody final UploadUrlRequest request) {
        final UploadUrlResponse data = videoService.createUploadUrl(request);
        return ResponseEntity.ok(EntityResponse.ok("Upload URL generated", data));
    }

    /** STEP 3: React saves metadata once the file is in R2. */
    @PostMapping
    public ResponseEntity<EntityResponse> create(
            @Valid @RequestBody final VideoMetadataRequest request) {
        final VideoResponse data = videoService.saveMetadata(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.created("Video created successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityResponse> getById(@PathVariable final UUID id) {
        final VideoResponse data = videoService.getById(id);
        return ResponseEntity.ok(EntityResponse.ok("Video fetched successfully", data));
    }

    @GetMapping
    public ResponseEntity<EntityResponse> getAll(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "createdAt") final String sortBy,
            @RequestParam(defaultValue = "desc") final String direction) {

        final Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        final Pageable pageable = PageRequest.of(page, size, sort);

        final Page<VideoResponse> result = videoService.getAll(pageable);
        final MetaData meta = new MetaData(result.getTotalPages(), result.getTotalElements());

        return ResponseEntity.ok(
                EntityResponse.ok("Videos fetched successfully", result.getContent(), meta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityResponse> update(@PathVariable final UUID id,
                                                 @Valid @RequestBody final VideoUpdateRequest request) {
        final VideoResponse data = videoService.update(id, request);
        return ResponseEntity.ok(EntityResponse.ok("Video updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityResponse> delete(@PathVariable final UUID id) {
        videoService.softDelete(id);
        return ResponseEntity.ok(EntityResponse.ok("Video deleted successfully", null));
    }
}
