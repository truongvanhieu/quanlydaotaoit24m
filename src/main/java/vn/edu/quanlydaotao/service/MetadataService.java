package vn.edu.quanlydaotao.service;

import java.util.List;
import org.springframework.stereotype.Service;
import vn.edu.quanlydaotao.dto.MetadataResponse;
import vn.edu.quanlydaotao.repository.CoursePrerequisiteRepository;

@Service
public class MetadataService {

	private final CoursePrerequisiteRepository prerequisiteRepository;

	public MetadataService(CoursePrerequisiteRepository prerequisiteRepository) {
		this.prerequisiteRepository = prerequisiteRepository;
	}

	public MetadataResponse getMetadata() {
		List<String> prerequisiteTypes = prerequisiteRepository.findDistinctTypes();
		if (!prerequisiteTypes.contains("REQUIRED")) {
			prerequisiteTypes = new java.util.ArrayList<>(prerequisiteTypes);
			prerequisiteTypes.add("REQUIRED");
			prerequisiteTypes.sort(String::compareToIgnoreCase);
		}
		return new MetadataResponse(
				List.of(),
				prerequisiteTypes,
				"Bảng courses hiện không có cột loại môn học; tính bắt buộc/tự chọn được lưu tại program_courses.is_compulsory.");
	}
}
