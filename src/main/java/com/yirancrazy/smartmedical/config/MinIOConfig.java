package com.yirancrazy.smartmedical.config;


import com.yirancrazy.smartmedical.utils.MinIOUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: MinIo 配置类
 * @DateTime: 2024/4/10 12:01
 * @Version: 1.0
 **/

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
@Slf4j
public class MinIOConfig {
    private String endpoint;
    private String fileHost;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private Integer imgSize;
    private Integer fileSize;

    @Bean
    public MinIOUtil createMinioClient() {
        return new MinIOUtil(endpoint, fileHost, bucketName, accessKey, secretKey, imgSize, fileSize);
    }

    /**
     * 容器启动完成后确保 MinIO bucket 存在，不存在则创建；失败则终止启动（快速失败）
     */
    @Bean
    public ApplicationRunner minioBucketInitializer() {
        return (ApplicationArguments args) -> {
            MinIOUtil.ensureBucketInitialized();
            log.info("MinIO bucket 就绪: {}", bucketName);
        };
    }
}

