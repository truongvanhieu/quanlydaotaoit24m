package vn.edu.quanlydaotao.config;

import java.io.IOException;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import vn.edu.quanlydaotao.exception.ApiError;

@Configuration
public class SecurityConfig {

	private final ObjectMapper objectMapper;

	public SecurityConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/**"))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
						.requestMatchers("/api/v1/**").hasRole("ADMIN")
						.anyRequest().permitAll())
				.exceptionHandling(exceptions -> exceptions
						.accessDeniedHandler((request, response, exception) -> writeError(
								request,
								response,
								HttpStatus.FORBIDDEN,
								"Tài khoản không có quyền quản trị.")))
				.httpBasic(basic -> basic.authenticationEntryPoint((request, response, exception) -> {
					response.setHeader("WWW-Authenticate", "Basic realm=\"EduManage\"");
					writeError(
							request,
							response,
							HttpStatus.UNAUTHORIZED,
							"Cần đăng nhập bằng tài khoản quản trị.");
				}));
		return http.build();
	}

	private void writeError(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpStatus status,
			String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(
				response.getOutputStream(),
				new ApiError(
						Instant.now(),
						status.value(),
						status.getReasonPhrase(),
						message,
						request.getRequestURI()));
	}
}
