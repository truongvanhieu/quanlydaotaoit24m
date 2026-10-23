package vn.edu.quanlydaotao.dto;

import vn.edu.quanlydaotao.entity.ProgramCourse;

public record ProgramCourseResponse(
		Long courseId,
		String courseCode,
		String courseName,
		int credits,
		int semesterIndex,
		boolean isCompulsory) {

	public static ProgramCourseResponse from(ProgramCourse programCourse) {
		return new ProgramCourseResponse(
				programCourse.getCourse().getId(),
				programCourse.getCourse().getCourseCode(),
				programCourse.getCourse().getCourseName(),
				programCourse.getCourse().getCredits(),
				programCourse.getSemesterIndex(),
				programCourse.isCompulsory());
	}
}
