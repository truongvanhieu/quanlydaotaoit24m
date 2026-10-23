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
import vn.edu.quanlydaotao.dto.MajorRequest;
import vn.edu.quanlydaotao.dto.MajorResponse;
import vn.edu.quanlydaotao.dto.PagedResponse;
import vn.edu.quanlydaotao.service.MajorService;

@Validated
@RestController
@RequestMapping("/api/v1/majors")
public class MajorController {

	private final MajorService majorService;

	public MajorController(MajorService majorService) {
		this.majorService = majorService;
	}

	@GetMapping
	public PagedResponse<MajorResponse> getMajors(
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải lớn hơn hoặc bằng 0") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "limit phải lớn hơn 0") @Max(value = 100, message = "limit tối đa là 100") int limit) {
		return majorService.getMajors(q, page, limit);
	}

	@GetMapping("/{id}")
	public MajorResponse getMajor(@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id) {
		return majorService.getMajor(id);
	}

	@PostMapping
	public ResponseEntity<MajorResponse> createMajor(@Valid @RequestBody MajorRequest request) {
		MajorResponse created = majorService.createMajor(request);
		return ResponseEntity.created(URI.create("/api/v1/majors/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	public MajorResponse updateMajor(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id,
			@Valid @RequestBody MajorRequest request) {
		return majorService.updateMajor(id, request);
	}

	@DeleteMapping("/{id}")
	public MajorResponse deactivateMajor(
			@PathVariable @Min(value = 1, message = "ID phải lớn hơn 0") Long id) {
		return majorService.deactivateMajor(id);
	}
}
