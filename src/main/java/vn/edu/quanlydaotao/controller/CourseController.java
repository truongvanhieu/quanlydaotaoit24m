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
import vn.edu.quanlydaotao.dto.CourseRequest;
import vn.edu.quanlydaotao.dto.CourseResponse;
import vn.edu.quanlydaotao.dto.PagedResponse;
import vn.edu.quanlydaotao.service.CourseService;

@Validated
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@GetMapping
	public PagedResponse<CourseResponse> getCourses(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) @Min(value = 0, message = "credits không được âm") Integer credits,
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải lớn hơn hoặc bằng 0") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "limit phải lớn hơn 0") @Max(value = 100, message = "limit tối đa là 100") int limit) {
		return courseService.getCourses(q, credits, page, limit);
	}

	@GetMapping("/{id}")
	public CourseResponse getCourse(@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id) {
		return courseService.getCourse(id);
	}

	@PostMapping
	public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
		CourseResponse created = courseService.createCourse(request);
		return ResponseEntity.created(URI.create("/api/v1/courses/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	public CourseResponse updateCourse(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id,
			@Valid @RequestBody CourseRequest request) {
		return courseService.updateCourse(id, request);
	}

	@DeleteMapping("/{id}")
	public CourseResponse deactivateCourse(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id) {
		return courseService.deactivateCourse(id);
	}
}
