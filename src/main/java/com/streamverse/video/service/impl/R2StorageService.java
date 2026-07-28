package com.streamverse.video.service.impl;

import com.streamverse.video.config.R2Properties;
import com.streamverse.video.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2StorageService implements StorageService {

    private final S3Client s3Client;
    private final R2Properties properties;
    private final S3Presigner s3Presigner;

    @Override
    public String upload(MultipartFile file) throws IOException {

        String key = generateObjectKey(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()
                )
        );

        return key;
    }

    @Override
    public InputStream download(String key) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .build();

        ResponseInputStream<?> response = s3Client.getObject(request);

        return response;
    }

    @Override
    public void delete(String key) {

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public boolean exists(String key) {

        try {

            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.headObject(request);

            return true;

        } catch (NoSuchKeyException e) {

            return false;

        } catch (Exception e) {

            return false;
        }
    }

    private String generateObjectKey(String originalFilename) {

        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return UUID.randomUUID() + extension;
    }

    @Override
    public String generateUploadUrl(String key) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    @Override
    public String generateUploadUrl(String key, String contentType) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    @Override
    public String getPublicUrl(String key) {

        if (key == null || key.isBlank()) {
            return null;
        }

        String base = properties.getPublicBaseUrl();

        if (base == null || base.isBlank()) {
            throw new IllegalStateException(
                    "cloudflare.r2.public-base-url is not configured");
        }

        return base.replaceAll("/+$", "") + "/" + key;
    }

    @Override
    public String generateDownloadUrl(String key) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
}
