package vn.edu.quanlydaotao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record CourseRequest(
		@NotBlank(message = "Mã môn học không được để trống")
		@Size(max = 20, message = "Mã môn học tối đa 20 ký tự")
		String courseCode,
		@NotBlank(message = "Tên môn học không được để trống")
		@Size(max = 200, message = "Tên môn học tối đa 200 ký tự")
		String courseName,
		@NotNull(message = "Số tín chỉ không được để trống")
		@Min(value = 0, message = "Số tín chỉ không được âm")
		Integer credits,
		@NotNull(message = "Tín chỉ lý thuyết không được để trống")
		@Min(value = 0, message = "Tín chỉ lý thuyết không được âm")
		Integer theoryCredits,
		@NotNull(message = "Tín chỉ thực hành không được để trống")
		@Min(value = 0, message = "Tín chỉ thực hành không được âm")
		Integer practiceCredits,
		String description,
		Boolean isActive) {

	@AssertTrue(message = "Tín chỉ lý thuyết và thực hành không được vượt quá tổng số tín chỉ")
	public boolean isCreditBreakdownValid() {
		return credits == null || theoryCredits == null || practiceCredits == null
				|| (long) theoryCredits + practiceCredits <= credits;
	}
}
