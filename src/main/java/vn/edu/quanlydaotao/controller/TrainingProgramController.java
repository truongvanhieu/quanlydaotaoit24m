package vn.edu.quanlydaotao.controller;

import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.quanlydaotao.dto.PagedResponse;
import vn.edu.quanlydaotao.dto.ProgramCourseRequest;
import vn.edu.quanlydaotao.dto.ProgramCourseResponse;
import vn.edu.quanlydaotao.dto.TrainingProgramDetail;
import vn.edu.quanlydaotao.dto.TrainingProgramRequest;
import vn.edu.quanlydaotao.dto.TrainingProgramSummary;
import vn.edu.quanlydaotao.service.TrainingProgramService;

@Validated
@RestController
@RequestMapping("/api/v1/training-programs")
public class TrainingProgramController {

	private final TrainingProgramService programService;

	public TrainingProgramController(TrainingProgramService programService) {
		this.programService = programService;
	}

	@GetMapping
	public PagedResponse<TrainingProgramSummary> getPrograms(
			@RequestParam(required = false) @Min(value = 1, message = "majorId phải lớn hơn 0") Long majorId,
			@RequestParam(required = false) String cohort,
			@RequestParam(required = false) String academicYear,
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải lớn hơn hoặc bằng 0") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "limit phải lớn hơn 0") @Max(value = 100, message = "limit tối đa là 100") int limit) {
		return programService.getPrograms(majorId, cohort, academicYear, q, page, limit);
	}

	@GetMapping("/{id}")
	public TrainingProgramDetail getProgram(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id) {
		return programService.getProgram(id);
	}

	@PostMapping
	public ResponseEntity<TrainingProgramSummary> createProgram(
			@Valid @RequestBody TrainingProgramRequest request) {
		TrainingProgramSummary created = programService.createProgram(request);
		return ResponseEntity.created(URI.create("/api/v1/training-programs/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	public TrainingProgramSummary updateProgram(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id,
			@Valid @RequestBody TrainingProgramRequest request) {
		return programService.updateProgram(id, request);
	}

	@DeleteMapping("/{id}")
	public TrainingProgramSummary deactivateProgram(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id) {
		return programService.deactivateProgram(id);
	}

	@PostMapping("/{id}/courses")
	public ResponseEntity<ProgramCourseResponse> assignCourse(
			@PathVariable @Min(value = 1, message = "ID chương trình phải lớn hơn 0") Long id,
			@Valid @RequestBody ProgramCourseRequest request) {
		ProgramCourseResponse assigned = programService.assignCourse(id, request);
		return ResponseEntity.created(URI.create(
				"/api/v1/training-programs/" + id + "/courses/" + assigned.courseId())).body(assigned);
	}

	@DeleteMapping("/{id}/courses/{courseId}")
	public ResponseEntity<Void> removeCourse(
			@PathVariable @Min(value = 1, message = "ID chương trình phải lớn hơn 0") Long id,
			@PathVariable @Min(value = 1, message = "ID môn học phải lớn hơn 0") Long courseId) {
		programService.removeCourse(id, courseId);
		return ResponseEntity.noContent().build();
	}
}
