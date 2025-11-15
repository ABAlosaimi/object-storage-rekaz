package com.rekaz.storage.rekaz_storage.Storage.Configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class S3Configs {
    
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String region;


    @Bean
    public WebClient s3WebClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer
            .defaultCodecs()
            .maxInMemorySize(100 * 1024 * 1024)) // 100MB for in-memory data buffering to serialize & deserialize responses/requests
            .build();
    }
    
    public String getHost() {
        return String.format("%s.s3.%s.amazonaws.com", bucketName, region);
    }
    
    public String getEndpoint() {
        return String.format("https://%s", getHost());
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
