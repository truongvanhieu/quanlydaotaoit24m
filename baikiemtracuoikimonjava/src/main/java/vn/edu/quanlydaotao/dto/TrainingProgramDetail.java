package vn.edu.quanlydaotao.dto;

import java.util.List;
import vn.edu.quanlydaotao.entity.TrainingProgram;

public record TrainingProgramDetail(
		Long id,
		String programCode,
		String programName,
		Long majorId,
		String majorCode,
		String majorName,
		String cohort,
		String academicYear,
		int totalCredits,
		String description,
		boolean isActive,
		List<ProgramCourseResponse> courses) {

	public static TrainingProgramDetail from(TrainingProgram program) {
		List<ProgramCourseResponse> courses = program.getProgramCourses().stream()
				.sorted((first, second) -> {
					int semesterOrder = Integer.compare(first.getSemesterIndex(), second.getSemesterIndex());
					return semesterOrder != 0
							? semesterOrder
							: first.getCourse().getCourseCode().compareToIgnoreCase(second.getCourse().getCourseCode());
				})
				.map(ProgramCourseResponse::from)
				.toList();
		int totalCredits = courses.stream()
				.mapToInt(ProgramCourseResponse::credits)
				.sum();

		return new TrainingProgramDetail(
				program.getId(),
				program.getProgramCode(),
				program.getProgramName(),
				program.getMajor().getId(),
				program.getMajor().getMajorCode(),
				program.getMajor().getMajorName(),
				program.getCohort(),
				program.getAcademicYear(),
				totalCredits,
				program.getDescription(),
				program.isActive(),
				courses);
	}
}
