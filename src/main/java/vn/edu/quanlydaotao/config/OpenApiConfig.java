package vn.edu.quanlydaotao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI trainingApi() {
		return new OpenAPI()
				.info(new Info()
						.title("EduManage API")
						.description("API quản lý ngành, chương trình đào tạo, môn học và môn tiên quyết.")
						.version("v1"));
	}
}
