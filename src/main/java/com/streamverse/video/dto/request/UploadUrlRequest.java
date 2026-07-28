package com.streamverse.video.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * STEP 1 request: React asks for a presigned URL to upload one file to R2.
 */
@Getter
@Setter
public class UploadUrlRequest {

    @NotBlank(message = "fileName is required")
    private String fileName;

    @NotBlank(message = "contentType is required")
    private String contentType;
}
