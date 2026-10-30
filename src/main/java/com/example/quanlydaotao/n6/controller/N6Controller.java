package com.example.quanlydaotao.n6.controller;

import com.example.quanlydaotao.n6.dto.EquivalenceRequest;
import com.example.quanlydaotao.n6.dto.PeriodRequest;
import com.example.quanlydaotao.n6.dto.RegisterRequest;
import com.example.quanlydaotao.n6.dto.RegistrationEligibilityResponse;
import com.example.quanlydaotao.n6.dto.RegistrationSummaryResponse;
import com.example.quanlydaotao.n6.entity.CourseEquivalence;
import com.example.quanlydaotao.n6.entity.CourseRegistration;
import com.example.quanlydaotao.n6.entity.RegistrationPeriod;
import com.example.quanlydaotao.n6.service.N6Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public REST endpoints for the shared University Training Management project.
 * Route names are resource-based; they are not tied to an internal /api/n6 prefix.
 */
@RestController
@Tag(name = "Course registration", description = "Đợt đăng ký, đăng ký học phần và môn tương đương")
public class N6Controller {
    private final N6Service service;

    public N6Controller(N6Service service) {
        this.service = service;
    }

    // Registration periods: /api/registration-periods
    @GetMapping("/api/registration-periods")
    @Operation(summary = "Danh sách đợt đăng ký", description = "Có thể lọc theo semesterId và active.")
    public List<RegistrationPeriod> periods(
            @RequestParam(name = "semesterId", required = false) UUID semesterId,
            @RequestParam(name = "active", required = false) Boolean active) {
        return service.periods(semesterId, active);
    }

    @GetMapping("/api/registration-periods/{id}")
    @Operation(summary = "Lấy chi tiết một đợt đăng ký")
    public RegistrationPeriod getPeriod(@PathVariable("id") UUID id) {
        return service.getPeriod(id);
    }

    @PostMapping("/api/registration-periods")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo đợt đăng ký")
    public RegistrationPeriod createPeriod(@RequestBody PeriodRequest request) {
        return service.savePeriod(null, request);
    }

    @PutMapping("/api/registration-periods/{id}")
    @Operation(summary = "Cập nhật đợt đăng ký")
    public RegistrationPeriod updatePeriod(@PathVariable("id") UUID id,
                                            @RequestBody PeriodRequest request) {
        return service.savePeriod(id, request);
    }

    @DeleteMapping("/api/registration-periods/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xóa mềm đợt đăng ký")
    public void deletePeriod(@PathVariable("id") UUID id) {
        service.deletePeriod(id);
    }

    // Course registrations: /api/course-registrations
    @GetMapping("/api/course-registrations")
    @Operation(summary = "Danh sách đăng ký học phần", description = "Có thể lọc theo sinh viên, đợt, lớp hoặc trạng thái.")
    public List<CourseRegistration> registrations(
            @RequestParam(name = "studentId", required = false) UUID studentId,
            @RequestParam(name = "registrationPeriodId", required = false) UUID registrationPeriodId,
            @RequestParam(name = "courseClassId", required = false) UUID courseClassId,
            @RequestParam(name = "status", required = false) Short status) {
        return service.registrations(studentId, registrationPeriodId, courseClassId, status);
    }

    @GetMapping("/api/course-registrations/{id}")
    @Operation(summary = "Chi tiết một đăng ký học phần")
    public CourseRegistration getRegistration(@PathVariable("id") UUID id) {
        return service.getRegistration(id);
    }

    @PostMapping("/api/course-registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đăng ký học phần", description = "Kiểm tra thời gian, tín chỉ, sức chứa, điều kiện học lại/cải thiện, tiên quyết và lịch học.")
    public CourseRegistration register(@RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @GetMapping("/api/course-registrations/eligibility")
    @Operation(summary = "Kiểm tra điều kiện trước khi đăng ký")
    public RegistrationEligibilityResponse eligibility(
            @RequestParam(name = "studentId") UUID studentId,
            @RequestParam(name = "courseClassId") UUID courseClassId,
            @RequestParam(name = "registrationPeriodId") UUID registrationPeriodId,
            @RequestParam(name = "registrationType") Short registrationType,
            @RequestParam(name = "replacedGradeId", required = false) UUID replacedGradeId) {
        return service.eligibility(new RegisterRequest(studentId, courseClassId,
                registrationPeriodId, registrationType, replacedGradeId));
    }

    @GetMapping("/api/course-registrations/summary")
    @Operation(summary = "Tổng hợp số môn và tín chỉ của sinh viên trong một đợt")
    public RegistrationSummaryResponse registrationSummary(
            @RequestParam(name = "studentId") UUID studentId,
            @RequestParam(name = "registrationPeriodId") UUID registrationPeriodId) {
        return service.registrationSummary(studentId, registrationPeriodId);
    }

    @PatchMapping("/api/course-registrations/{id}/cancel")
    @Operation(summary = "Hủy đăng ký học phần")
    public CourseRegistration cancel(@PathVariable("id") UUID id) {
        return service.cancel(id);
    }

    // Course equivalences: /api/course-equivalences
    @GetMapping("/api/course-equivalences")
    @Operation(summary = "Danh sách môn học tương đương", description = "Có thể lọc theo courseId hoặc active.")
    public List<CourseEquivalence> equivalences(
            @RequestParam(name = "courseId", required = false) UUID courseId,
            @RequestParam(name = "active", required = false) Boolean active) {
        return service.equivalences(courseId, active);
    }

    @GetMapping("/api/course-equivalences/{id}")
    @Operation(summary = "Chi tiết một quan hệ môn tương đương")
    public CourseEquivalence getEquivalence(@PathVariable("id") UUID id) {
        return service.getEquivalence(id);
    }

    @PostMapping("/api/course-equivalences")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo quan hệ môn tương đương")
    public CourseEquivalence createEquivalence(@RequestBody EquivalenceRequest request) {
        return service.saveEquivalence(null, request);
    }

    @PutMapping("/api/course-equivalences/{id}")
    @Operation(summary = "Cập nhật quan hệ môn tương đương")
    public CourseEquivalence updateEquivalence(@PathVariable("id") UUID id,
                                               @RequestBody EquivalenceRequest request) {
        return service.saveEquivalence(id, request);
    }

    @DeleteMapping("/api/course-equivalences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xóa mềm quan hệ môn tương đương")
    public void deleteEquivalence(@PathVariable("id") UUID id) {
        service.deleteEquivalence(id);
    }

    // Lookups belong to the shared API as resources, not /api/n6 internal lookups.
    @GetMapping("/api/registration-options/semesters")
    @Operation(summary = "Danh sách học kỳ đang hoạt động")
    public List<Map<String, Object>> semesters() {
        return service.semesters();
    }

    @GetMapping("/api/registration-options/course-classes")
    @Operation(summary = "Danh sách lớp học phần đang hoạt động")
    public List<Map<String, Object>> classes(
            @RequestParam(name = "semesterId", required = false) UUID semesterId) {
        return service.classes(semesterId);
    }

    @GetMapping("/api/registration-options/courses")
    @Operation(summary = "Danh sách môn học đang hoạt động")
    public List<Map<String, Object>> courses() {
        return service.courses();
    }

    @GetMapping("/api/registration-options/students")
    @Operation(summary = "Danh sách sinh viên đang hoạt động")
    public List<Map<String, Object>> students() {
        return service.students();
    }
}
