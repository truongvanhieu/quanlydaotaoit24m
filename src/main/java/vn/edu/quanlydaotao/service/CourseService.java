package vn.edu.quanlydaotao.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.quanlydaotao.dto.CourseRequest;
import vn.edu.quanlydaotao.dto.CourseResponse;
import vn.edu.quanlydaotao.dto.PagedResponse;
import vn.edu.quanlydaotao.entity.Course;
import vn.edu.quanlydaotao.exception.ConflictException;
import vn.edu.quanlydaotao.exception.NotFoundException;
import vn.edu.quanlydaotao.repository.CourseRepository;

@Service
@Transactional(readOnly = true)
public class CourseService {

	private final CourseRepository courseRepository;

	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public PagedResponse<CourseResponse> getCourses(String query, Integer credits, int page, int limit) {
		String keyword = query == null || query.isBlank() ? null : query.trim();
		PageRequest pageable = PageRequest.of(
				page,
				limit,
				Sort.by(Sort.Direction.ASC, "courseCode"));
		Page<CourseResponse> result = courseRepository.search(keyword, credits, pageable)
				.map(CourseResponse::from);
		return PagedResponse.from(result);
	}

	public CourseResponse getCourse(Long id) {
		return CourseResponse.from(findCourse(id));
	}

	@Transactional
	public CourseResponse createCourse(CourseRequest request) {
		String courseCode = request.courseCode().trim();
		if (courseRepository.existsByCourseCodeIgnoreCase(courseCode)) {
			throw new ConflictException("Mã môn học đã tồn tại.");
		}

		Course course = new Course();
		applyRequest(course, request, true);
		return CourseResponse.from(courseRepository.save(course));
	}

	@Transactional
	public CourseResponse updateCourse(Long id, CourseRequest request) {
		Course course = findCourse(id);
		String courseCode = request.courseCode().trim();
		if (!course.getCourseCode().equalsIgnoreCase(courseCode)
				&& courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(courseCode, id)) {
			throw new ConflictException("Mã môn học đã tồn tại.");
		}

		applyRequest(course, request, false);
		return CourseResponse.from(courseRepository.save(course));
	}

	@Transactional
	public CourseResponse deactivateCourse(Long id) {
		Course course = findCourse(id);
		course.setActive(false);
		return CourseResponse.from(courseRepository.save(course));
	}

	private Course findCourse(Long id) {
		return courseRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Không tìm thấy môn học với ID " + id + "."));
	}

	private void applyRequest(Course course, CourseRequest request, boolean creating) {
		course.setCourseCode(request.courseCode().trim());
		course.setCourseName(request.courseName().trim());
		course.setCredits(request.credits());
		course.setTheoryCredits(request.theoryCredits());
		course.setPracticeCredits(request.practiceCredits());
		course.setDescription(request.description());
		if (request.isActive() != null) {
			course.setActive(request.isActive());
		} else if (creating) {
			course.setActive(true);
		}
	}
}
