package vn.edu.quanlydaotao.dto;

import java.util.List;

public record MetadataResponse(
		List<String> courseTypes,
		List<String> prerequisiteTypes,
		String courseTypeNote) {
}
