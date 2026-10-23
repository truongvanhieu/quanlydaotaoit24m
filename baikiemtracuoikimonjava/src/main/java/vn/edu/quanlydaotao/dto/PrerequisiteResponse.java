package vn.edu.quanlydaotao.dto;

import vn.edu.quanlydaotao.entity.CoursePrerequisite;

public record PrerequisiteResponse(
		Long prerequisiteCourseId,
		String courseCode,
		String courseName,
		String type) {

	public static PrerequisiteResponse from(CoursePrerequisite prerequisite) {
		return new PrerequisiteResponse(
				prerequisite.getPrerequisiteCourse().getId(),
				prerequisite.getPrerequisiteCourse().getCourseCode(),
				prerequisite.getPrerequisiteCourse().getCourseName(),
				prerequisite.getType());
	}
}
