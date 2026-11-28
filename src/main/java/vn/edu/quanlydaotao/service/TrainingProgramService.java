package vn.edu.quanlydaotao.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.quanlydaotao.dto.PagedResponse;
import vn.edu.quanlydaotao.dto.ProgramCourseRequest;
import vn.edu.quanlydaotao.dto.ProgramCourseResponse;
import vn.edu.quanlydaotao.dto.TrainingProgramDetail;
import vn.edu.quanlydaotao.dto.TrainingProgramRequest;
import vn.edu.quanlydaotao.dto.TrainingProgramSummary;
import vn.edu.quanlydaotao.entity.Course;
import vn.edu.quanlydaotao.entity.CoursePrerequisite;
import vn.edu.quanlydaotao.entity.Major;
import vn.edu.quanlydaotao.entity.ProgramCourse;
import vn.edu.quanlydaotao.entity.TrainingProgram;
import vn.edu.quanlydaotao.exception.ConflictException;
import vn.edu.quanlydaotao.exception.NotFoundException;
import vn.edu.quanlydaotao.repository.CoursePrerequisiteRepository;
import vn.edu.quanlydaotao.repository.CourseRepository;
import vn.edu.quanlydaotao.repository.MajorRepository;
import vn.edu.quanlydaotao.repository.ProgramCourseRepository;
import vn.edu.quanlydaotao.repository.TrainingProgramRepository;

@Service
@Transactional(readOnly = true)
public class TrainingProgramService {

	private final TrainingProgramRepository programRepository;
	private final MajorRepository majorRepository;
	private final CourseRepository courseRepository;
	private final ProgramCourseRepository programCourseRepository;
	private final CoursePrerequisiteRepository prerequisiteRepository;

	public TrainingProgramService(
			TrainingProgramRepository programRepository,
			MajorRepository majorRepository,
			CourseRepository courseRepository,
			ProgramCourseRepository programCourseRepository,
			CoursePrerequisiteRepository prerequisiteRepository) {
		this.programRepository = programRepository;
		this.majorRepository = majorRepository;
		this.courseRepository = courseRepository;
		this.programCourseRepository = programCourseRepository;
		this.prerequisiteRepository = prerequisiteRepository;
	}

	public PagedResponse<TrainingProgramSummary> getPrograms(
			Long majorId,
			String cohort,
			String academicYear,
			String query,
			int page,
			int limit) {
		PageRequest pageable = PageRequest.of(
				page,
				limit,
				Sort.by(Sort.Direction.ASC, "programCode"));
		Page<TrainingProgramSummary> result = programRepository.search(
						majorId,
						normalize(cohort),
						normalize(academicYear),
						normalize(query),
						pageable)
				.map(TrainingProgramSummary::from);
		return PagedResponse.from(result);
	}

	public TrainingProgramDetail getProgram(Long id) {
		return TrainingProgramDetail.from(findDetailedProgram(id));
	}

	@Transactional
	public TrainingProgramSummary createProgram(TrainingProgramRequest request) {
		if (programRepository.existsByProgramCodeIgnoreCase(request.programCode().trim())) {
			throw new ConflictException("Mã chương trình đào tạo đã tồn tại.");
		}

		Major major = findMajor(request.majorId());
		TrainingProgram program = new TrainingProgram();
		applyRequest(program, request, major, true);
		return TrainingProgramSummary.from(programRepository.save(program));
	}

	@Transactional
	public TrainingProgramSummary updateProgram(Long id, TrainingProgramRequest request) {
		TrainingProgram program = findProgram(id);
		String programCode = request.programCode().trim();
		if (!program.getProgramCode().equalsIgnoreCase(programCode)
				&& programRepository.existsByProgramCodeIgnoreCaseAndIdNot(programCode, id)) {
			throw new ConflictException("Mã chương trình đào tạo đã tồn tại.");
		}

		Major major = findMajor(request.majorId());
		applyRequest(program, request, major, false);
		return TrainingProgramSummary.from(programRepository.save(program));
	}

	@Transactional
	public TrainingProgramSummary deactivateProgram(Long id) {
		TrainingProgram program = findProgram(id);
		program.setActive(false);
		return TrainingProgramSummary.from(programRepository.save(program));
	}

	@Transactional
	public ProgramCourseResponse assignCourse(Long programId, ProgramCourseRequest request) {
		TrainingProgram program = findProgram(programId);
		Course course = courseRepository.findById(request.courseId())
				.orElseThrow(() -> new NotFoundException(
						"Không tìm thấy môn học với ID " + request.courseId() + "."));
		if (programCourseRepository.existsByProgram_IdAndCourse_Id(programId, course.getId())) {
			throw new ConflictException("Môn học này đã được gán vào chương trình.");
		}
		validateSemesterOrder(programId, course, request.semesterIndex());

		ProgramCourse programCourse = new ProgramCourse();
		programCourse.setProgram(program);
		programCourse.setCourse(course);
		programCourse.setSemesterIndex(request.semesterIndex());
		programCourse.setCompulsory(request.isCompulsory());
		ProgramCourseResponse response = ProgramCourseResponse.from(programCourseRepository.save(programCourse));
		refreshTotalCredits(program);
		return response;
	}

	@Transactional
	public ProgramCourseResponse updateCourseAssignment(Long programId, Long courseId, ProgramCourseRequest request) {
		findProgram(programId);
		ProgramCourse programCourse = programCourseRepository
				.findByProgram_IdAndCourse_Id(programId, courseId)
				.orElseThrow(() -> new NotFoundException(
						"Môn học này chưa được gán vào chương trình hoặc không tồn tại."));
		validateSemesterOrder(programId, programCourse.getCourse(), request.semesterIndex());
		programCourse.setSemesterIndex(request.semesterIndex());
		programCourse.setCompulsory(request.isCompulsory());
		return ProgramCourseResponse.from(programCourseRepository.save(programCourse));
	}

	@Transactional
	public void removeCourse(Long programId, Long courseId) {
		TrainingProgram program = findProgram(programId);
		ProgramCourse programCourse = programCourseRepository
				.findByProgram_IdAndCourse_Id(programId, courseId)
				.orElseThrow(() -> new NotFoundException(
						"Môn học này chưa được gán vào chương trình hoặc không tồn tại."));
		programCourseRepository.delete(programCourse);
		refreshTotalCredits(program);
	}

	private void validateSemesterOrder(Long programId, Course course, int semesterIndex) {
		for (CoursePrerequisite relation : prerequisiteRepository
				.findByCourse_IdOrderByPrerequisiteCourse_CourseCode(course.getId())) {
			Course prereqCourse = relation.getPrerequisiteCourse();
			programCourseRepository.findByProgram_IdAndCourse_Id(programId, prereqCourse.getId())
					.ifPresent(prereqPc -> {
						if (prereqPc.getSemesterIndex() >= semesterIndex) {
							throw new ConflictException(
									"Không thể xếp môn " + course.getCourseCode()
											+ " ở Học kỳ " + semesterIndex
											+ " vì môn tiên quyết " + prereqCourse.getCourseCode()
											+ " đang ở Học kỳ " + prereqPc.getSemesterIndex()
											+ " (phải học trước).");
						}
					});
		}

		for (CoursePrerequisite relation : prerequisiteRepository.findByPrerequisiteCourse_Id(course.getId())) {
			Course dependentCourse = relation.getCourse();
			programCourseRepository.findByProgram_IdAndCourse_Id(programId, dependentCourse.getId())
					.ifPresent(dependentPc -> {
						if (dependentPc.getSemesterIndex() <= semesterIndex) {
							throw new ConflictException(
									"Không thể xếp môn " + course.getCourseCode()
											+ " ở Học kỳ " + semesterIndex
											+ " vì môn này là tiên quyết của môn " + dependentCourse.getCourseCode()
											+ " đang ở Học kỳ " + dependentPc.getSemesterIndex()
											+ " (phải học trước).");
						}
					});
		}
	}

	private void refreshTotalCredits(TrainingProgram program) {
		long totalCredits = programCourseRepository.sumCourseCreditsByProgramId(program.getId());
		program.setTotalCredits(Math.toIntExact(totalCredits));
		programRepository.save(program);
	}

	private void applyRequest(
			TrainingProgram program,
			TrainingProgramRequest request,
			Major major,
			boolean creating) {
		program.setProgramCode(request.programCode().trim());
		program.setProgramName(request.programName().trim());
		program.setMajor(major);
		program.setCohort(trimToNull(request.cohort()));
		program.setAcademicYear(trimToNull(request.academicYear()));
		program.setDescription(request.description());
		if (request.isActive() != null) {
			program.setActive(request.isActive());
		} else if (creating) {
			program.setActive(true);
		}
	}

	private TrainingProgram findProgram(Long id) {
		return programRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(
						"Không tìm thấy chương trình đào tạo với ID " + id + "."));
	}

	private TrainingProgram findDetailedProgram(Long id) {
		return programRepository.findDetailedById(id)
				.orElseThrow(() -> new NotFoundException(
						"Không tìm thấy chương trình đào tạo với ID " + id + "."));
	}

	private Major findMajor(Long id) {
		return majorRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Không tìm thấy ngành đào tạo với ID " + id + "."));
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
