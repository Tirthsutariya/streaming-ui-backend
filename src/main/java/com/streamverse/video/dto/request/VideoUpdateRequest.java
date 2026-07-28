package com.streamverse.video.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for a full update (PUT) of an existing video.
 */
@Getter
@Setter
public class VideoUpdateRequest {

    @NotBlank(message = "description is required")
    @Size(max = 100, message = "description must not exceed 100 characters")
    private String description;

    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    private String thumbnail;

    @Size(max = 100, message = "videoLink must not exceed 100 characters")
    private String videoLink;
}
