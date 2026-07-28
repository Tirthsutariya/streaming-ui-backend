package com.streamverse.video.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloudflare.r2")
public class R2Properties {

    private String accountId;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    // Public base URL for reading objects back (R2 public bucket or custom
    // domain), e.g. https://pub-xxxx.r2.dev or https://cdn.yoursite.com
    private String publicBaseUrl;

}
