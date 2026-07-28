package com.streamverse.video.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class VideoResponse {

    private UUID id;
    private String description;
    private String name;
    private String thumbnail;
    private String videoLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}
