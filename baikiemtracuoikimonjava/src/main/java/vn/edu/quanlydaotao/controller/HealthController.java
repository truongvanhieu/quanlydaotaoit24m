package vn.edu.quanlydaotao.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

	private final JdbcTemplate jdbcTemplate;

	public HealthController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/health")
	public HealthResponse health() {
		String database = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
		return new HealthResponse("UP", database);
	}

	public record HealthResponse(String status, String database) {
	}
}
