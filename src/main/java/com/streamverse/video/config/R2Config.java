package com.streamverse.video.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    private final R2Properties properties;

    @Bean
    public S3Client s3Client() {

        return S3Client.builder()
                .endpointOverride(
                        URI.create(
                                "https://"
                                        + properties.getAccountId()
                                        + ".r2.cloudflarestorage.com"
                        )
                )
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        properties.getAccessKey(),
                                        properties.getSecretKey()
                                )
                        )
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {

        return S3Presigner.builder()
                .endpointOverride(
                        URI.create(
                                "https://" +
                                        properties.getAccountId() +
                                        ".r2.cloudflarestorage.com"
                        )
                )
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        properties.getAccessKey(),
                                        properties.getSecretKey()
                                )
                        )
                )
                .build();
    }
}
