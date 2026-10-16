package com.example.quanlydaotao.n2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HỆ THỐNG QUẢN LÝ ĐÀO TẠO - NHÓM 2: QUẢN LÝ SINH VIÊN")
                        .description("Tài liệu API RESTful cho Phân hệ Quản lý Sinh viên (Nhóm II) bao gồm 6 bảng: " +
                                     "students, student_classes, student_classe_sections, " +
                                     "advisor_classe_sections, student_status, student_status_histories.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nhóm 2 - Quản lý sinh viên")
                                .email("nhom2@it24m.edu.vn")));
    }
}
