package com.streamverse.video.service.impl;

import com.streamverse.video.dto.request.UploadUrlRequest;
import com.streamverse.video.dto.request.VideoMetadataRequest;
import com.streamverse.video.dto.request.VideoUpdateRequest;
import com.streamverse.video.constant.VideoStatus;
import com.streamverse.video.dto.response.UploadUrlResponse;
import com.streamverse.video.dto.response.VideoResponse;
import com.streamverse.video.entity.Video;
import com.streamverse.video.exception.DuplicateResourceException;
import com.streamverse.video.exception.ResourceNotFoundException;
import com.streamverse.video.mapper.VideoMapper;
import com.streamverse.video.repository.VideoRepository;
import com.streamverse.video.service.StorageService;
import com.streamverse.video.service.TranscodingService;
import com.streamverse.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    private final StorageService storageService;
    private final TranscodingService transcodingService;

    @Override
    public UploadUrlResponse createUploadUrl(final UploadUrlRequest request) {
        final String key = buildObjectKey(request.getFileName());
        final String uploadUrl = storageService.generateUploadUrl(key, request.getContentType());
        return UploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .key(key)
                .build();
    }

    @Override
    @Transactional
    public VideoResponse saveMetadata(final VideoMetadataRequest request) {
        if (videoRepository.existsByDescriptionAndIsDeletedFalse(request.getDescription())) {
            throw new DuplicateResourceException(
                    "A video with description '" + request.getDescription() + "' already exists");
        }

        final Video video = Video.builder()
                .id(UUID.randomUUID())
                .description(request.getDescription())
                .name(request.getName())
                .videoLink(storageService.getPublicUrl(request.getVideoKey()))
                .thumbnail(storageService.getPublicUrl(request.getThumbnailKey()))
                .status(VideoStatus.PROCESSING)
                .build();

        final Video saved = videoRepository.save(video);

        // Fire-and-forget: generate the HLS ladder in the background. The row
        // starts as PROCESSING and flips to READY (or FAILED) when done.
        transcodingService.transcodeToHls(saved.getId(), request.getVideoKey());

        return videoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoResponse getById(final UUID id) {
        return videoMapper.toResponse(findActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoResponse> getAll(final Pageable pageable) {
        return videoRepository.findAllByIsDeletedFalse(pageable)
                .map(videoMapper::toResponse);
    }

    @Override
    @Transactional
    public VideoResponse update(final UUID id, final VideoUpdateRequest request) {
        final Video video = findActive(id);

        if (videoRepository.existsByDescriptionAndIsDeletedFalseAndIdNot(request.getDescription(), id)) {
            throw new DuplicateResourceException(
                    "A video with description '" + request.getDescription() + "' already exists");
        }

        video.setDescription(request.getDescription());
        video.setName(request.getName());
        video.setThumbnail(request.getThumbnail());
        video.setVideoLink(request.getVideoLink());

        return videoMapper.toResponse(videoRepository.save(video));
    }

    @Override
    @Transactional
    public void softDelete(final UUID id) {
        final Video video = findActive(id);
        video.setIsDeleted(true);
        videoRepository.save(video);
    }

    private Video findActive(final UUID id) {
        return videoRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
    }

    /** Random, collision-free object key that preserves the original extension. */
    private String buildObjectKey(final String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}
