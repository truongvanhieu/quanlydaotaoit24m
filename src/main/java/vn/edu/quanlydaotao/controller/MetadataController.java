package vn.edu.quanlydaotao.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.quanlydaotao.dto.MetadataResponse;
import vn.edu.quanlydaotao.service.MetadataService;

@RestController
@RequestMapping("/api/v1")
public class MetadataController {

	private final MetadataService metadataService;

	public MetadataController(MetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@GetMapping("/metadata")
	public MetadataResponse getMetadata() {
		return metadataService.getMetadata();
	}
}
