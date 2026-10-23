package vn.edu.quanlydaotao.dto;

import vn.edu.quanlydaotao.entity.Course;

public record CourseResponse(
		Long id,
		String courseCode,
		String courseName,
		int credits,
		int theoryCredits,
		int practiceCredits,
		String description,
		boolean isActive) {

	public static CourseResponse from(Course course) {
		return new CourseResponse(
				course.getId(),
				course.getCourseCode(),
				course.getCourseName(),
				course.getCredits(),
				course.getTheoryCredits(),
				course.getPracticeCredits(),
				course.getDescription(),
				course.isActive());
	}
}
