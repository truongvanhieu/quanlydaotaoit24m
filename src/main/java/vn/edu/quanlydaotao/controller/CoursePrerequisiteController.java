package vn.edu.quanlydaotao.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.quanlydaotao.dto.PrerequisiteRequest;
import vn.edu.quanlydaotao.dto.PrerequisiteResponse;
import vn.edu.quanlydaotao.service.CoursePrerequisiteService;

@Validated
@RestController
@RequestMapping("/api/v1/courses/{id}/prerequisites")
public class CoursePrerequisiteController {

	private final CoursePrerequisiteService prerequisiteService;

	public CoursePrerequisiteController(CoursePrerequisiteService prerequisiteService) {
		this.prerequisiteService = prerequisiteService;
	}

	@GetMapping
	public List<PrerequisiteResponse> getPrerequisites(
			@PathVariable @Min(value = 1, message = "ID môn học phải lớn hơn 0") Long id) {
		return prerequisiteService.getPrerequisites(id);
	}

	@PostMapping
	public ResponseEntity<PrerequisiteResponse> addPrerequisite(
			@PathVariable @Min(value = 1, message = "ID môn học phải lớn hơn 0") Long id,
			@Valid @RequestBody PrerequisiteRequest request) {
		PrerequisiteResponse created = prerequisiteService.addPrerequisite(id, request);
		return ResponseEntity.created(URI.create(
				"/api/v1/courses/" + id + "/prerequisites/" + created.prerequisiteCourseId())).body(created);
	}

	@DeleteMapping("/{prerequisiteId}")
	public ResponseEntity<Void> removePrerequisite(
			@PathVariable @Min(value = 1, message = "ID môn học phải lớn hơn 0") Long id,
			@PathVariable @Min(value = 1, message = "ID môn tiên quyết phải lớn hơn 0") Long prerequisiteId) {
		prerequisiteService.removePrerequisite(id, prerequisiteId);
		return ResponseEntity.noContent().build();
	}
}
