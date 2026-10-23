package vn.edu.quanlydaotao.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.quanlydaotao.dto.CoursePrerequisiteEdge;
import vn.edu.quanlydaotao.dto.PrerequisiteRequest;
import vn.edu.quanlydaotao.dto.PrerequisiteResponse;
import vn.edu.quanlydaotao.entity.Course;
import vn.edu.quanlydaotao.entity.CoursePrerequisite;
import vn.edu.quanlydaotao.exception.BadRequestException;
import vn.edu.quanlydaotao.exception.ConflictException;
import vn.edu.quanlydaotao.exception.NotFoundException;
import vn.edu.quanlydaotao.repository.CoursePrerequisiteRepository;
import vn.edu.quanlydaotao.repository.CourseRepository;

@Service
@Transactional
public class CoursePrerequisiteService {

	private final CoursePrerequisiteRepository prerequisiteRepository;
	private final CourseRepository courseRepository;

	public CoursePrerequisiteService(
			CoursePrerequisiteRepository prerequisiteRepository,
			CourseRepository courseRepository) {
		this.prerequisiteRepository = prerequisiteRepository;
		this.courseRepository = courseRepository;
	}

	public List<PrerequisiteResponse> getPrerequisites(Long courseId) {
		findCourse(courseId);
		return prerequisiteRepository.findByCourse_IdOrderByPrerequisiteCourse_CourseCode(courseId).stream()
				.map(PrerequisiteResponse::from)
				.toList();
	}

	public PrerequisiteResponse addPrerequisite(Long courseId, PrerequisiteRequest request) {
		Course course = findCourse(courseId);
		Course prerequisiteCourse = findCourse(request.prerequisiteCourseId());
		if (courseId.equals(prerequisiteCourse.getId())) {
			throw new BadRequestException("Môn học không thể là môn tiên quyết của chính nó.");
		}
		if (prerequisiteRepository.existsByCourse_IdAndPrerequisiteCourse_Id(courseId, prerequisiteCourse.getId())) {
			throw new ConflictException("Quan hệ tiên quyết này đã tồn tại.");
		}
		if (wouldCreateCycle(courseId, prerequisiteCourse.getId())) {
			throw new ConflictException("Quan hệ này tạo thành chu trình môn tiên quyết.");
		}

		CoursePrerequisite prerequisite = new CoursePrerequisite();
		prerequisite.setCourse(course);
		prerequisite.setPrerequisiteCourse(prerequisiteCourse);
		prerequisite.setType(request.type().trim().toUpperCase(Locale.ROOT));
		return PrerequisiteResponse.from(prerequisiteRepository.save(prerequisite));
	}

	public void removePrerequisite(Long courseId, Long prerequisiteCourseId) {
		findCourse(courseId);
		CoursePrerequisite prerequisite = prerequisiteRepository
				.findByCourse_IdAndPrerequisiteCourse_Id(courseId, prerequisiteCourseId)
				.orElseThrow(() -> new NotFoundException(
						"Không tìm thấy quan hệ tiên quyết cần gỡ."));
		prerequisiteRepository.delete(prerequisite);
	}

	private boolean wouldCreateCycle(Long courseId, Long prerequisiteCourseId) {
		Map<Long, List<Long>> graph = new HashMap<>();
		for (CoursePrerequisiteEdge edge : prerequisiteRepository.findAllEdges()) {
			graph.computeIfAbsent(edge.courseId(), ignored -> new java.util.ArrayList<>())
					.add(edge.prerequisiteCourseId());
		}

		ArrayDeque<Long> pending = new ArrayDeque<>();
		Set<Long> visited = new HashSet<>();
		pending.add(prerequisiteCourseId);
		while (!pending.isEmpty()) {
			Long current = pending.removeFirst();
			if (current.equals(courseId)) {
				return true;
			}
			if (visited.add(current)) {
				pending.addAll(graph.getOrDefault(current, List.of()));
			}
		}
		return false;
	}

	private Course findCourse(Long id) {
		return courseRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Không tìm thấy môn học với ID " + id + "."));
	}
}
