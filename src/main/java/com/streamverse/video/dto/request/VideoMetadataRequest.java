package com.streamverse.video.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * STEP 3 request: after the file is in R2, React sends the metadata plus the
 * object key(s) returned in STEP 1 to persist the record.
 */
@Getter
@Setter
public class VideoMetadataRequest {

    @NotBlank(message = "description is required")
    @Size(max = 100, message = "description must not exceed 100 characters")
    private String description;

    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "videoKey is required")
    private String videoKey;

    private String thumbnailKey;
}
