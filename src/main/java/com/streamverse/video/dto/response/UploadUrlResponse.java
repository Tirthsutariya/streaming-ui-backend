package com.streamverse.video.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * STEP 1 response: the presigned PUT URL R2 will accept, and the object key
 * React must send back in STEP 3.
 */
@Getter
@Setter
@Builder
public class UploadUrlResponse {

    private String uploadUrl;
    private String key;
}
