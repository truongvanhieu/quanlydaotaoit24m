package vn.edu.quanlydaotao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProgramCourseRequest(
		@NotNull(message = "Môn học không được để trống")
		@Positive(message = "ID môn học phải lớn hơn 0")
		Long courseId,
		@NotNull(message = "Học kỳ không được để trống")
		@Positive(message = "Học kỳ phải lớn hơn 0")
		Integer semesterIndex,
		@NotNull(message = "Trạng thái bắt buộc/tự chọn không được để trống")
		Boolean isCompulsory) {
}
