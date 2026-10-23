package vn.edu.quanlydaotao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TrainingProgramRequest(
		@NotBlank(message = "Mã chương trình không được để trống")
		@Size(max = 30, message = "Mã chương trình tối đa 30 ký tự")
		String programCode,
		@NotBlank(message = "Tên chương trình không được để trống")
		@Size(max = 200, message = "Tên chương trình tối đa 200 ký tự")
		String programName,
		@NotNull(message = "Ngành đào tạo không được để trống")
		@Positive(message = "ID ngành phải lớn hơn 0")
		Long majorId,
		@Size(max = 20, message = "Khóa học tối đa 20 ký tự")
		String cohort,
		@Size(max = 20, message = "Năm học tối đa 20 ký tự")
		String academicYear,
		String description,
		Boolean isActive) {
}
