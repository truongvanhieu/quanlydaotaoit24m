package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.entity.LecturerCourseClass;
import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.LecturerCourseClassRepository;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CourseSectionService {
    private final CourseSectionRepository repo;
    private final SemesterRepository semRepo;
    private final StudentCourseSectionRepository scsRepo;
    private final LecturerCourseClassRepository lccRepo;
    private final N5CatalogService catalogService;

    public CourseSectionService(CourseSectionRepository repo,
                                SemesterRepository semRepo,
                                StudentCourseSectionRepository scsRepo,
                                LecturerCourseClassRepository lccRepo,
                                N5CatalogService catalogService) {
        this.repo = repo;
        this.semRepo = semRepo;
        this.scsRepo = scsRepo;
        this.lccRepo = lccRepo;
        this.catalogService = catalogService;
    }

    public List<CourseSection> findAll(Boolean isActive) {
        return findAll(null, null, null, isActive);
    }

    public List<CourseSection> findAll(String keyword, UUID semesterId, String status, Boolean isActive) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String st = (status != null && !status.isBlank()) ? status.trim() : null;
        List<CourseSection> list = repo.search(kw, semesterId, st, isActive);
        enrichCourseSections(list);
        return list;
    }

    private void enrichCourseSections(List<CourseSection> list) {
        if (list == null || list.isEmpty()) return;
        Map<String, String> courseMap = catalogService.getCourseNameMap();
        Map<String, String> roomMap = catalogService.getRoomNameMap();
        Map<String, String> lecturerMap = catalogService.getLecturerNameMap();

        Map<UUID, LecturerCourseClass> assignmentMap = new HashMap<>();
        for (LecturerCourseClass lcc : lccRepo.findByDeletedAtIsNull()) {
            if (lcc.getCourseClassId() != null && Boolean.TRUE.equals(lcc.getIsActive())) {
                assignmentMap.putIfAbsent(lcc.getCourseClassId(), lcc);
            }
        }

        for (CourseSection cs : list) {
            if ((cs.getCourseName() == null || cs.getCourseName().isBlank()) && cs.getCourseId() != null) {
                cs.setCourseName(courseMap.getOrDefault(cs.getCourseId().toString().toLowerCase(), cs.getName()));
            }
            if ((cs.getRoomName() == null || cs.getRoomName().isBlank()) && cs.getRoomId() != null) {
                cs.setRoomName(roomMap.getOrDefault(cs.getRoomId().toString().toLowerCase(), "Phòng học"));
            }
            if ((cs.getLecturerName() == null || cs.getLecturerName().isBlank())) {
                if (cs.getLecturerId() != null) {
                    cs.setLecturerName(lecturerMap.getOrDefault(cs.getLecturerId().toString().toLowerCase(), null));
                }
                if ((cs.getLecturerName() == null || cs.getLecturerName().isBlank()) && cs.getId() != null) {
                    LecturerCourseClass assigned = assignmentMap.get(cs.getId());
                    if (assigned != null) {
                        cs.setLecturerId(assigned.getEmployeeId());
                        cs.setLecturerName(assigned.getEmployeeName());
                    }
                }
            }
            if (cs.getId() != null) {
                cs.setCurrentStudents(scsRepo.countByCourseSectionIdAndDeletedAtIsNull(cs.getId()));
            }
        }
    }

    public CourseSection findById(UUID id) {
        CourseSection cs = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần với ID: " + id));
        enrichCourseSections(List.of(cs));
        return cs;
    }

    private String generateSectionCode(CourseSection e, Semester sem) {
        String coursePrefix = "UDA";
        if (e.getCourseId() != null) {
            String cname = catalogService.getCourseNameMap().get(e.getCourseId().toString().toLowerCase());
            if (cname != null && cname.contains("-")) {
                coursePrefix = cname.split("-")[0].trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            }
        }
        if (coursePrefix.isBlank()) coursePrefix = "UDA";

        String semPart = "2026";
        if (sem != null && sem.getCode() != null && !sem.getCode().isBlank()) {
            semPart = sem.getCode().trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        } else if (sem != null && sem.getStartDate() != null) {
            semPart = String.valueOf(sem.getStartDate().getYear());
        }

        String basePrefix = coursePrefix + "-" + semPart + "-";
        int maxSeq = 0;
        for (CourseSection existing : repo.findAll()) {
            String c = existing.getCode();
            if (c != null && c.toUpperCase().startsWith(basePrefix.toUpperCase())) {
                String tail = c.substring(basePrefix.length()).replaceAll("[^0-9]", "");
                if (!tail.isEmpty()) {
                    try {
                        maxSeq = Math.max(maxSeq, Integer.parseInt(tail));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("%s%02d", basePrefix, maxSeq + 1);
    }

    public CourseSection save(CourseSection e, UUID semId) {
        if (e.getName() == null || e.getName().isBlank()) {
            throw new IllegalArgumentException("Tên lớp học phần không được để trống!");
        }
        if (e.getMaxStudents() != null && e.getMaxStudents() <= 0) {
            throw new IllegalArgumentException("Sĩ số tối đa phải lớn hơn 0!");
        }
        if (e.getRegistrationStart() != null && e.getRegistrationEnd() != null && !e.getRegistrationStart().isBefore(e.getRegistrationEnd())) {
            throw new IllegalArgumentException("Thời gian bắt đầu lớp học phần phải nhỏ hơn thời gian kết thúc!");
        }
        Semester sem = null;
        UUID targetSemId = semId != null ? semId : e.getSemesterId();
        if (targetSemId != null) {
            sem = semRepo.findById(targetSemId).orElse(null);
            if (sem != null) e.setSemester(sem);
        }
        resolveMetadata(e);
        if (e.getCode() == null || e.getCode().isBlank()) {
            e.setCode(generateSectionCode(e, sem));
        }
        CourseSection saved = repo.save(e);
        syncLecturerAssignment(saved);
        enrichCourseSections(List.of(saved));
        return saved;
    }

    private void resolveMetadata(CourseSection e) {
        if (e.getCourseId() != null && (e.getCourseName() == null || e.getCourseName().isBlank())) {
            e.setCourseName(catalogService.getCourseNameMap().get(e.getCourseId().toString().toLowerCase()));
        }
        if (e.getRoomId() != null && (e.getRoomName() == null || e.getRoomName().isBlank())) {
            e.setRoomName(catalogService.getRoomNameMap().get(e.getRoomId().toString().toLowerCase()));
        }
        if (e.getLecturerId() != null && (e.getLecturerName() == null || e.getLecturerName().isBlank())) {
            e.setLecturerName(catalogService.getLecturerNameMap().get(e.getLecturerId().toString().toLowerCase()));
        }
    }

    private void syncLecturerAssignment(CourseSection cs) {
        if (cs.getLecturerId() != null && cs.getId() != null) {
            List<LecturerCourseClass> existing = lccRepo.findExistingAssignment(cs.getLecturerId(), cs.getId());
            if (existing.isEmpty()) {
                LecturerCourseClass lcc = new LecturerCourseClass();
                lcc.setEmployeeId(cs.getLecturerId());
                lcc.setEmployeeName(cs.getLecturerName());
                lcc.setCourseSection(cs);
                lcc.setCourseClassName(cs.getName());
                lcc.setRole("Giảng viên chính");
                lcc.setHours(45);
                lcc.setIsActive(true);
                lccRepo.save(lcc);
            }
        }
    }

    public CourseSection update(UUID id, CourseSection updateData, UUID semId) {
        CourseSection existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần với ID: " + id));
        if (updateData.getMaxStudents() != null && updateData.getMaxStudents() <= 0) {
            throw new RuntimeException("Sĩ số tối đa phải lớn hơn 0!");
        }
        if (updateData.getCode() != null) existing.setCode(updateData.getCode());
        if (updateData.getName() != null) existing.setName(updateData.getName());
        if (updateData.getCourseId() != null) existing.setCourseId(updateData.getCourseId());
        if (updateData.getCourseName() != null) existing.setCourseName(updateData.getCourseName());
        if (updateData.getLecturerId() != null) existing.setLecturerId(updateData.getLecturerId());
        if (updateData.getLecturerName() != null) existing.setLecturerName(updateData.getLecturerName());
        if (updateData.getRoomId() != null) existing.setRoomId(updateData.getRoomId());
        if (updateData.getRoomName() != null) existing.setRoomName(updateData.getRoomName());
        if (updateData.getBuildingId() != null) existing.setBuildingId(updateData.getBuildingId());
        if (updateData.getMaxStudents() != null) existing.setMaxStudents(updateData.getMaxStudents());
        if (updateData.getMinStudents() != null) existing.setMinStudents(updateData.getMinStudents());
        if (updateData.getClassType() != null) existing.setClassType(updateData.getClassType());
        if (updateData.getStatus() != null) existing.setStatus(updateData.getStatus());
        if (updateData.getRegistrationStart() != null) existing.setRegistrationStart(updateData.getRegistrationStart());
        if (updateData.getRegistrationEnd() != null) existing.setRegistrationEnd(updateData.getRegistrationEnd());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        UUID targetSemId = semId != null ? semId : updateData.getSemesterId();
        if (targetSemId != null) {
            Semester sem = semRepo.findById(targetSemId).orElse(null);
            if (sem != null) existing.setSemester(sem);
        }
        resolveMetadata(existing);
        CourseSection saved = repo.save(existing);
        syncLecturerAssignment(saved);
        enrichCourseSections(List.of(saved));
        return saved;
    }

    public CourseSection assignLecturer(UUID sectionId, UUID employeeId, String role, Integer hours, Boolean isActive) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Vui lòng chọn giảng viên giảng dạy!");
        }
        if (hours != null && hours <= 0) {
            throw new IllegalArgumentException("Số tiết phân công phải lớn hơn 0!");
        }
        CourseSection cs = repo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần với ID: " + sectionId));

        String lecturerName = catalogService.getLecturerNameMap()
                .getOrDefault(employeeId.toString().toLowerCase(), "Giảng viên");
        String assignedRole = (role != null && !role.isBlank()) ? role.trim() : "Giảng viên chính";
        int assignedHours = (hours != null && hours > 0) ? hours : 45;
        boolean assignedActive = isActive == null || Boolean.TRUE.equals(isActive);

        if (assignedActive) {
            cs.setLecturerId(employeeId);
            cs.setLecturerName(lecturerName);
        }
        CourseSection saved = repo.save(cs);

        List<LecturerCourseClass> existing = lccRepo.findExistingAssignment(employeeId, sectionId);
        if (existing.isEmpty()) {
            LecturerCourseClass lcc = new LecturerCourseClass();
            lcc.setEmployeeId(employeeId);
            lcc.setEmployeeName(lecturerName);
            lcc.setCourseSection(saved);
            lcc.setCourseClassName(saved.getName());
            lcc.setRole(assignedRole);
            lcc.setHours(assignedHours);
            lcc.setIsActive(assignedActive);
            lccRepo.save(lcc);
        } else {
            LecturerCourseClass current = existing.get(0);
            current.setEmployeeName(lecturerName);
            current.setRole(assignedRole);
            current.setHours(assignedHours);
            current.setIsActive(assignedActive);
            lccRepo.save(current);
        }

        enrichCourseSections(List.of(saved));
        return saved;
    }

    public CourseSection toggleActive(UUID id, Boolean isActive) {
        CourseSection existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần với ID: " + id));
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        CourseSection saved = repo.save(existing);
        enrichCourseSections(List.of(saved));
        return saved;
    }

    public void delete(UUID id) {
        CourseSection existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần với ID: " + id));
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
