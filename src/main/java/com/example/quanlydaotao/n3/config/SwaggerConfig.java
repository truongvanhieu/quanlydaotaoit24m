
package com.example.quanlydaotao.n3.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi n3Api() {

        return GroupedOpenApi.builder()

                // Tên nhóm API
                .group("n3")

                // Chỉ lấy API thuộc nhóm N3
                .pathsToMatch("/api/n3/**")

                // Chỉ quét Controller của N3
                .packagesToScan(
                    "com.example.quanlydaotao.n3.controller"
                )

                .build();
    }
}
