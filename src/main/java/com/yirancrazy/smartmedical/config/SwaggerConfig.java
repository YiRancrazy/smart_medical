package com.yirancrazy.smartmedical.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: Swagger 配置类
 * @DateTime: 2024/4/10 12:01
 * @Version: 1.0
 **/

@Configuration
public class SwaggerConfig {
    /**
     * 根据@Tag 上的排序，写入x-order
     *
     * @return the global open api customizer
     */
    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            // 核心目标：遍历所有 Path，并将 SecurityRequirement 添加到每个 Operation 中
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {

                    // 排除不需要认证的路径（可选，可自定义配置实现）
                    // if (isPathExcluded(path)) { return; }

                    // 遍历该路径下的所有 HTTP 操作（GET, POST, PUT, DELETE...）
                    pathItem.readOperations().forEach(operation -> {

                        // 1. 创建操作级别的安全要求
                        SecurityRequirement securityRequirement = new SecurityRequirement()
                                .addList("Bearer Authentication");

                        // 2. 将安全要求添加到 Operation 对象中
                        operation.addSecurityItem(securityRequirement);
                    });
                });
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智能医疗预约挂号系统 API")
                        .version("1.0")
                        .description("面向 admin / doctor / user 三端的医院预约挂号与后台管理接口；鉴权统一使用 Bearer JWT。")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }


}
