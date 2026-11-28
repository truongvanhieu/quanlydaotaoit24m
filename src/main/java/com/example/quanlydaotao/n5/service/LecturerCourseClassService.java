package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.entity.LecturerCourseClass;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.LecturerCourseClassRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LecturerCourseClassService {
    private final LecturerCourseClassRepository repo;
    private final CourseSectionRepository csRepo;
    private final N5CatalogService catalogService;

    public LecturerCourseClassService(LecturerCourseClassRepository repo,
                                      CourseSectionRepository csRepo,
                                      N5CatalogService catalogService) {
        this.repo = repo;
        this.csRepo = csRepo;
        this.catalogService = catalogService;
    }

    public List<LecturerCourseClass> findAll(Boolean isActive) {
        return findAll(null, null, null, null, null, isActive);
    }

    public List<LecturerCourseClass> findAll(UUID employeeId, UUID courseClassId, String employeeName, String courseClassName, String role, Boolean isActive) {
        String empName = (employeeName != null && !employeeName.isBlank()) ? employeeName.trim() : null;
        String clsName = (courseClassName != null && !courseClassName.isBlank()) ? courseClassName.trim() : null;
        String r = (role != null && !role.isBlank()) ? role.trim() : null;
        List<LecturerCourseClass> list = repo.search(employeeId, courseClassId, empName, clsName, r, isActive);
        enrichAssignments(list);
        return list;
    }

    private void enrichAssignments(List<LecturerCourseClass> list) {
        if (list == null || list.isEmpty()) return;
        Map<String, String> lecturerMap = catalogService.getLecturerNameMap();
        for (LecturerCourseClass item : list) {
            if ((item.getEmployeeName() == null || item.getEmployeeName().isBlank()) && item.getEmployeeId() != null) {
                item.setEmployeeName(lecturerMap.getOrDefault(item.getEmployeeId().toString().toLowerCase(), "Giảng viên"));
            }
            if ((item.getCourseClassName() == null || item.getCourseClassName().isBlank()) && item.getCourseSection() != null) {
                item.setCourseClassName(item.getCourseSection().getName());
            }
        }
    }

    public LecturerCourseClass findById(UUID id) {
        LecturerCourseClass item = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phân công với ID: " + id));
        enrichAssignments(List.of(item));
        return item;
    }

    public LecturerCourseClass save(LecturerCourseClass e, UUID csId) {
        UUID targetCsId = csId != null ? csId : e.getCourseClassId();
        UUID targetEmpId = e.getEmployeeId();
        if (targetEmpId == null && (e.getEmployeeName() == null || e.getEmployeeName().isBlank())) {
            throw new IllegalArgumentException("Giảng viên không được để trống!");
        }
        if (targetCsId == null && (e.getCourseClassName() == null || e.getCourseClassName().isBlank())) {
            throw new IllegalArgumentException("Lớp học phần không được để trống!");
        }
        if (e.getHours() != null && e.getHours() <= 0) {
            throw new IllegalArgumentException("Số tiết phân công phải lớn hơn 0!");
        }

        if (targetEmpId != null && targetCsId != null) {
            List<LecturerCourseClass> dup = repo.findExistingAssignment(targetEmpId, targetCsId);
            if (!dup.isEmpty()) {
                throw new RuntimeException("Giảng viên đã được phân công vào lớp học phần này rồi!");
            }
        }

        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId).orElse(null);
            if (cs != null) {
                e.setCourseSection(cs);
                if (e.getCourseClassName() == null || e.getCourseClassName().isBlank()) {
                    e.setCourseClassName(cs.getName());
                }
            }
        }

        if (targetEmpId != null && (e.getEmployeeName() == null || e.getEmployeeName().isBlank())) {
            String resolvedName = catalogService.getLecturerNameMap().get(targetEmpId.toString().toLowerCase());
            if (resolvedName != null) {
                e.setEmployeeName(resolvedName);
            }
        }

        LecturerCourseClass saved = repo.save(e);
        enrichAssignments(List.of(saved));
        return saved;
    }

    public LecturerCourseClass update(UUID id, LecturerCourseClass updateData, UUID csId) {
        LecturerCourseClass existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phân công với ID: " + id));
        if (updateData.getEmployeeId() != null) {
            existing.setEmployeeId(updateData.getEmployeeId());
            if (updateData.getEmployeeName() == null || updateData.getEmployeeName().isBlank()) {
                String resolvedName = catalogService.getLecturerNameMap().get(updateData.getEmployeeId().toString().toLowerCase());
                if (resolvedName != null) existing.setEmployeeName(resolvedName);
            }
        }
        if (updateData.getEmployeeName() != null && !updateData.getEmployeeName().isBlank()) {
            existing.setEmployeeName(updateData.getEmployeeName());
        }
        if (updateData.getRole() != null) existing.setRole(updateData.getRole());
        if (updateData.getHours() != null) existing.setHours(updateData.getHours());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetCsId = csId != null ? csId : updateData.getCourseClassId();
        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId).orElse(null);
            if (cs != null) {
                existing.setCourseSection(cs);
                existing.setCourseClassName(cs.getName());
            }
        } else if (updateData.getCourseClassName() != null && !updateData.getCourseClassName().isBlank()) {
            existing.setCourseClassName(updateData.getCourseClassName());
        }

        LecturerCourseClass saved = repo.save(existing);
        enrichAssignments(List.of(saved));
        return saved;
    }

    public LecturerCourseClass toggleActive(UUID id, Boolean isActive) {
        LecturerCourseClass existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phân công với ID: " + id));
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        LecturerCourseClass saved = repo.save(existing);
        enrichAssignments(List.of(saved));
        return saved;
    }

    public void delete(UUID id) {
        LecturerCourseClass existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phân công với ID: " + id));
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
