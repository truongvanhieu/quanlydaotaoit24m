package vn.edu.quanlydaotao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PrerequisiteRequest(
		@NotNull(message = "Môn tiên quyết không được để trống")
		@Positive(message = "ID môn tiên quyết phải lớn hơn 0")
		Long prerequisiteCourseId,
		@NotBlank(message = "Loại quan hệ không được để trống")
		@Size(max = 30, message = "Loại quan hệ tối đa 30 ký tự")
		String type) {
}
