package vn.edu.quanlydaotao.dto;

import vn.edu.quanlydaotao.entity.TrainingProgram;

public record TrainingProgramSummary(
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
		boolean isActive) {

	public static TrainingProgramSummary from(TrainingProgram program) {
		int totalCredits = program.getProgramCourses().stream()
				.mapToInt(programCourse -> programCourse.getCourse().getCredits())
				.sum();
		return new TrainingProgramSummary(
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
				program.isActive());
	}
}
