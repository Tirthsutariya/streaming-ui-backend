package com.streamverse.video.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    String upload(MultipartFile file) throws IOException;

    InputStream download(String key);

    void delete(String key);

    boolean exists(String key);

    String generateUploadUrl(String key);

    /** Presigned PUT URL bound to the given content type (STEP 1). */
    String generateUploadUrl(String key, String contentType);

    String generateDownloadUrl(String key);

    /** Stable public URL for an object, built from the configured public base URL. */
    String getPublicUrl(String key);
}