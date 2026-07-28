package com.streamverse.video.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for creating a video.
 */
@Getter
@Setter
public class VideoRequest {

    @NotBlank
    @Size(max = 100)
    private String description;

    @Size(max = 100)
    private String name;
}
