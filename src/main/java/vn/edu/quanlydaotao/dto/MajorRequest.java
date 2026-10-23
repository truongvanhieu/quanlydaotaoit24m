package vn.edu.quanlydaotao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MajorRequest(
		@NotBlank(message = "Mã ngành không được để trống")
		@Size(max = 20, message = "Mã ngành tối đa 20 ký tự")
		String majorCode,
		@NotBlank(message = "Tên ngành không được để trống")
		@Size(max = 150, message = "Tên ngành tối đa 150 ký tự")
		String majorName,
		String description,
		Boolean isActive) {
}
