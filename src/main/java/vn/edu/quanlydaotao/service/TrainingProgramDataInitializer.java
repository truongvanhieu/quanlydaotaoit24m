package vn.edu.quanlydaotao.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.edu.quanlydaotao.entity.Major;
import vn.edu.quanlydaotao.entity.TrainingProgram;
import vn.edu.quanlydaotao.repository.MajorRepository;
import vn.edu.quanlydaotao.repository.TrainingProgramRepository;

@Component
@Order(2)
public class TrainingProgramDataInitializer implements ApplicationRunner {

	private static final String COHORT = "2026";
	private static final String ACADEMIC_YEAR = "2026-2030";

	private final MajorRepository majorRepository;
	private final TrainingProgramRepository programRepository;

	public TrainingProgramDataInitializer(
			MajorRepository majorRepository,
			TrainingProgramRepository programRepository) {
		this.majorRepository = majorRepository;
		this.programRepository = programRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		Set<String> existingProgramCodes = programRepository.findAll().stream()
				.map(program -> normalizeCode(program.getProgramCode()))
				.collect(Collectors.toSet());
		List<TrainingProgram> missingPrograms = majorRepository.findAll().stream()
				.filter(major -> INITIAL_MAJOR_CODES.contains(normalizeCode(major.getMajorCode())))
				.filter(major -> !existingProgramCodes.contains(normalizeCode(major.getMajorCode())))
				.map(TrainingProgramDataInitializer::toEntity)
				.toList();
		if (!missingPrograms.isEmpty()) {
			programRepository.saveAll(missingPrograms);
		}
	}

	private static TrainingProgram toEntity(Major major) {
		TrainingProgram program = new TrainingProgram();
		program.setProgramCode(major.getMajorCode());
		program.setProgramName(major.getMajorName());
		program.setMajor(major);
		program.setCohort(COHORT);
		program.setAcademicYear(ACADEMIC_YEAR);
		program.setTotalCredits(0);
		program.setDescription(major.getDescription());
		program.setActive(true);
		return program;
	}

	private static String normalizeCode(String code) {
		return code.toUpperCase(Locale.ROOT).replace('Ð', 'Đ');
	}

	private static final Set<String> INITIAL_MAJOR_CODES = Set.of(
			"CNTT", "QTKD", "LOG", "KT", "MKT", "NNA", "TMĐT", "TKDH");
}
