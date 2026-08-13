package com.streamverse.video.mapper;

import com.streamverse.video.dto.response.VideoResponse;
import com.streamverse.video.entity.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {

    public VideoResponse toResponse(final Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .description(video.getDescription())
                .name(video.getName())
                .thumbnail(video.getThumbnail())
                .videoLink(video.getVideoLink())
                .hlsUrl(video.getHlsUrl())
                .likes(video.getLikes())
                .status(video.getStatus())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .isDeleted(video.getIsDeleted())
                .build();
    }
}
