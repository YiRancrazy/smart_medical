package com.yirancrazy.smartmedical.config;


import com.yirancrazy.smartmedical.utils.MinIOUtil;
import lombok.Data;
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
        return new MinIOUtil(endpoint, bucketName, accessKey, secretKey, imgSize, fileSize);
    }
}

