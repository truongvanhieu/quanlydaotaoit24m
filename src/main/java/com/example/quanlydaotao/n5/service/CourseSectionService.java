package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.repository.CourseClassInstructorRepository;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseSectionService {

    private final CourseSectionRepository repository;
    private final SemesterRepository semesterRepository;
    private final StudentCourseSectionRepository registrationRepository;
    private final CourseClassInstructorRepository instructorRepository;

    public List<CourseSection> getAll(String keyword, UUID semesterId) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        if (normalizedKeyword != null || semesterId != null) {
            return repository.search(normalizedKeyword, semesterId);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public CourseSection getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
    }

    public CourseSection create(CourseSection request) {
        validate(request);
        if (repository.existsByCode(request.getCode().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã lớp học phần đã tồn tại");
        }
        request.setCode(request.getCode().trim());
        request.setName(request.getName().trim());
        request.setCreatedAt(java.time.LocalDateTime.now());
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            request.setStatus("open");
        }
        if (request.getIsActive() == null) {
            request.setIsActive(true);
        }
        return repository.save(request);
    }

    public CourseSection update(UUID id, CourseSection request) {
        validate(request);
        CourseSection existing = getById(id);
        String code = request.getCode().trim();
        if (!code.equals(existing.getCode()) && repository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã lớp học phần đã tồn tại");
        }
        existing.setCode(code);
        existing.setName(request.getName().trim());
        existing.setCourseId(request.getCourseId());
        existing.setSemesterId(request.getSemesterId());
        existing.setRoomId(request.getRoomId());
        existing.setBuildingId(request.getBuildingId());
        existing.setMaxStudents(request.getMaxStudents());
        existing.setMinStudents(request.getMinStudents());
        existing.setClassType(request.getClassType());
        existing.setStatus(request.getStatus());
        existing.setRegistrationStart(request.getRegistrationStart());
        existing.setRegistrationEnd(request.getRegistrationEnd());
        existing.setNote(request.getNote());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        return repository.save(existing);
    }

    private void validate(CourseSection request) {
        if (request.getCode() == null || request.getCode().isBlank()
                || request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mã và tên lớp học phần là bắt buộc");
        }
        if ((request.getMaxStudents() != null && request.getMaxStudents() < 0)
                || (request.getMinStudents() != null && request.getMinStudents() < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sĩ số không được âm");
        }
        if (request.getMinStudents() != null && request.getMaxStudents() != null
                && request.getMinStudents() > request.getMaxStudents()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sĩ số tối thiểu không được lớn hơn sĩ số tối đa");
        }
        if (request.getSemesterId() != null && !semesterRepository.existsById(request.getSemesterId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Học kỳ không tồn tại");
        }
        if (request.getRegistrationStart() != null && request.getRegistrationEnd() != null
                && request.getRegistrationStart().isAfter(request.getRegistrationEnd())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Thời gian bắt đầu đăng ký không được sau thời gian kết thúc");
        }
    }

    public void delete(UUID id) {
        CourseSection existing = getById(id);
        if (registrationRepository.existsByCourseSectionId(id)
                || instructorRepository.existsByCourseClassId(id)
                || repository.hasSchedule(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Không thể xóa lớp đang có đăng ký, lịch học hoặc phân công giảng viên");
        }
        repository.delete(existing);
    }
}
