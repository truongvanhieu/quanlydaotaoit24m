package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.repository.StudentRepository;
import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.entity.StudentCourseSection;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StudentCourseSectionService {
    private final StudentCourseSectionRepository repo;
    private final CourseSectionRepository csRepo;
    private final CourseSectionService courseSectionService;
    private final StudentRepository studentRepo;

    public StudentCourseSectionService(StudentCourseSectionRepository repo,
                                       CourseSectionRepository csRepo,
                                       CourseSectionService courseSectionService,
                                       StudentRepository studentRepo) {
        this.repo = repo;
        this.csRepo = csRepo;
        this.courseSectionService = courseSectionService;
        this.studentRepo = studentRepo;
    }

    public List<StudentCourseSection> findAll(Boolean isActive) {
        return findAll(null, null, null, isActive);
    }

    public List<StudentCourseSection> findAll(UUID studentId, UUID courseSectionId, String status, Boolean isActive) {
        String st = (status != null && !status.isBlank()) ? status.trim() : null;
        List<StudentCourseSection> list = repo.search(studentId, courseSectionId, st, isActive);
        enrichRegistrations(list);
        return list;
    }

    public Map<String, Object> getBySection(UUID sectionId) {
        CourseSection cs = courseSectionService.findById(sectionId);
        List<StudentCourseSection> regs = repo.findByCourseSectionIdAndDeletedAtIsNull(sectionId);
        enrichRegistrations(regs);

        Map<UUID, Student> studentMap = new HashMap<>();
        try {
            for (Student s : studentRepo.findAll()) {
                if (s.getId() != null) studentMap.put(s.getId(), s);
            }
        } catch (Exception ignored) {}

        long totalStudying = regs.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .filter(r -> {
                    String st = r.getStatus() != null ? r.getStatus().trim() : "";
                    return st.equalsIgnoreCase("Đang học") || st.equalsIgnoreCase("STUDYING");
                })
                .count();

        List<Map<String, Object>> studentList = new ArrayList<>();
        for (StudentCourseSection r : regs) {
            Student st = r.getStudentId() != null ? studentMap.get(r.getStudentId()) : null;
            String code = (r.getStudentCode() != null && !r.getStudentCode().isBlank())
                    ? r.getStudentCode()
                    : (st != null ? st.getStudentCode() : "—");
            String fullName = (r.getStudentName() != null && !r.getStudentName().isBlank())
                    ? r.getStudentName()
                    : (st != null ? st.getFullName() : "—");
            String email = st != null && st.getEmail() != null ? st.getEmail() : "—";

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("student_id", r.getStudentId());
            item.put("studentId", r.getStudentId());
            item.put("student_code", code);
            item.put("studentCode", code);
            item.put("full_name", fullName);
            item.put("fullName", fullName);
            item.put("studentName", fullName);
            item.put("email", email);
            item.put("registered_at", r.getRegistrationDate());
            item.put("registrationDate", r.getRegistrationDate());
            item.put("registration_type", r.getRegistrationType());
            item.put("registrationType", r.getRegistrationType());
            item.put("status", r.getStatus());
            item.put("is_active", r.getIsActive());
            item.put("isActive", r.getIsActive());
            studentList.add(item);
        }

        Map<String, Object> sectionInfo = new LinkedHashMap<>();
        sectionInfo.put("id", cs.getId());
        sectionInfo.put("code", cs.getCode());
        sectionInfo.put("name", cs.getName());
        sectionInfo.put("display_name", cs.getDisplayName());
        sectionInfo.put("displayName", cs.getDisplayName());
        sectionInfo.put("course_name", cs.getCourseName());
        sectionInfo.put("semester_name", cs.getSemesterShortName());
        sectionInfo.put("lecturer_name", cs.getLecturerName() != null ? cs.getLecturerName() : "Chưa phân công");
        sectionInfo.put("room_name", cs.getRoomName() != null ? cs.getRoomName() : "Chưa xếp phòng");
        sectionInfo.put("max_students", cs.getMaxStudents() != null ? cs.getMaxStudents() : 50);
        sectionInfo.put("maxStudents", cs.getMaxStudents() != null ? cs.getMaxStudents() : 50);
        sectionInfo.put("status", cs.getStatus());
        sectionInfo.put("is_active", cs.getIsActive());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("section_info", sectionInfo);
        result.put("total_studying", totalStudying);
        result.put("totalStudying", totalStudying);
        result.put("total_registered", regs.size());
        result.put("totalRegistered", regs.size());
        result.put("students", studentList);
        return result;
    }

    private void enrichRegistrations(List<StudentCourseSection> list) {
        if (list == null || list.isEmpty()) return;
        Map<UUID, Student> studentMap = new HashMap<>();
        try {
            for (Student s : studentRepo.findAll()) {
                if (s.getId() != null) studentMap.put(s.getId(), s);
            }
        } catch (Exception ignored) {}

        for (StudentCourseSection item : list) {
            if (item.getStudentId() != null && (item.getStudentName() == null || item.getStudentName().isBlank() || item.getStudentCode() == null || item.getStudentCode().isBlank())) {
                Student st = studentMap.get(item.getStudentId());
                if (st != null) {
                    if (item.getStudentName() == null || item.getStudentName().isBlank()) {
                        item.setStudentName(st.getFullName());
                    }
                    if (item.getStudentCode() == null || item.getStudentCode().isBlank()) {
                        item.setStudentCode(st.getStudentCode());
                    }
                }
            }
        }
    }

    public StudentCourseSection findById(UUID id) {
        StudentCourseSection item = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký với ID: " + id));
        enrichRegistrations(List.of(item));
        return item;
    }

    public StudentCourseSection save(StudentCourseSection e, UUID csId) {
        UUID targetCsId = csId != null ? csId : e.getCourseSectionId();
        if (e.getStudentId() == null || targetCsId == null) {
            throw new IllegalArgumentException("Sinh viên và Lớp học phần không được để trống!");
        }

        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần!"));

            if (Boolean.FALSE.equals(cs.getIsActive())) {
                throw new RuntimeException("Lớp học phần đang ngừng hoạt động, không thể đăng ký!");
            }

            if (cs.getStatus() != null) {
                String st = cs.getStatus().toLowerCase();
                if (st.contains("đóng") || st.equals("closed") || st.equals("canceled") || st.contains("hủy")) {
                    throw new RuntimeException("Lớp học phần này hiện đã đóng đăng ký!");
                }
            }

            if (e.getStudentId() != null && repo.existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(e.getStudentId(), targetCsId)) {
                throw new RuntimeException("Sinh viên đã đăng ký lớp học phần này rồi!");
            }

            long currentCount = repo.countByCourseSectionIdAndDeletedAtIsNull(targetCsId);
            if (cs.getMaxStudents() != null && currentCount >= cs.getMaxStudents()) {
                throw new RuntimeException("Lớp học phần đã đầy (Tối đa: " + cs.getMaxStudents() + " sinh viên)!");
            }

            e.setCourseSection(cs);
        }

        if (e.getStudentId() != null) {
            studentRepo.findById(e.getStudentId()).ifPresent(st -> {
                if (e.getStudentName() == null || e.getStudentName().isBlank()) {
                    e.setStudentName(st.getFullName());
                }
                if (e.getStudentCode() == null || e.getStudentCode().isBlank()) {
                    e.setStudentCode(st.getStudentCode());
                }
            });
        }

        StudentCourseSection saved = repo.save(e);
        enrichRegistrations(List.of(saved));
        return saved;
    }

    public Map<String, Object> saveBatch(UUID courseSectionId, List<UUID> studentIds, String registrationType, String status, String note, LocalDateTime registrationDate) {
        if (courseSectionId == null) {
            throw new IllegalArgumentException("Lớp học phần không được để trống!");
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 sinh viên để thêm vào lớp học phần!");
        }

        CourseSection cs = csRepo.findById(courseSectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần!"));

        if (Boolean.FALSE.equals(cs.getIsActive())) {
            throw new IllegalArgumentException("Lớp học phần đang ngừng hoạt động, không thể thêm sinh viên!");
        }
        if (cs.getStatus() != null) {
            String st = cs.getStatus().toLowerCase();
            if (st.contains("đóng") || st.equals("closed") || st.equals("canceled") || st.contains("hủy")) {
                throw new IllegalArgumentException("Lớp học phần này hiện đã đóng đăng ký!");
            }
        }

        int maxStudents = cs.getMaxStudents() != null ? cs.getMaxStudents() : 50;
        long currentCount = repo.countByCourseSectionIdAndDeletedAtIsNull(courseSectionId);

        String regType = (registrationType != null && !registrationType.isBlank()) ? registrationType.trim() : "Học lần đầu";
        String regStatus = (status != null && !status.isBlank()) ? status.trim() : "Đang học";

        Set<UUID> uniqueIds = new LinkedHashSet<>(studentIds);
        int addedCount = 0;
        int skippedDuplicates = 0;
        int skippedFull = 0;

        for (UUID studentId : uniqueIds) {
            if (studentId == null) continue;
            if (repo.existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(studentId, courseSectionId)) {
                skippedDuplicates++;
                continue;
            }
            if (currentCount >= maxStudents) {
                skippedFull++;
                continue;
            }
            StudentCourseSection item = new StudentCourseSection();
            item.setStudentId(studentId);
            item.setCourseSection(cs);
            item.setRegistrationDate(registrationDate != null ? registrationDate : LocalDateTime.now());
            item.setRegistrationType(regType);
            item.setStatus(regStatus);
            item.setNote(note);
            item.setIsActive(true);
            studentRepo.findById(studentId).ifPresent(st -> {
                item.setStudentName(st.getFullName());
                item.setStudentCode(st.getStudentCode());
            });
            repo.save(item);
            currentCount++;
            addedCount++;
        }

        if (addedCount == 0 && skippedFull > 0) {
            throw new IllegalArgumentException("Lớp học phần đã đạt sĩ số tối đa (" + maxStudents + " sinh viên)!");
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Đã thêm thành công ").append(addedCount).append(" sinh viên vào lớp học phần");
        if (skippedDuplicates > 0) {
            msg.append(" (bỏ qua ").append(skippedDuplicates).append(" SV đã có trong lớp)");
        }
        if (skippedFull > 0) {
            msg.append(" (").append(skippedFull).append(" SV không thể thêm do lớp đã đủ sĩ số ").append(maxStudents).append(")");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("addedCount", addedCount);
        result.put("skippedDuplicates", skippedDuplicates);
        result.put("skippedFull", skippedFull);
        result.put("currentStudents", currentCount);
        result.put("maxStudents", maxStudents);
        result.put("message", msg.toString());
        return result;
    }

    public StudentCourseSection update(UUID id, StudentCourseSection updateData, UUID csId) {
        StudentCourseSection existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký với ID: " + id));
        UUID targetCsId = csId != null ? csId : updateData.getCourseSectionId();
        UUID targetStudentId = updateData.getStudentId() != null ? updateData.getStudentId() : existing.getStudentId();

        if (targetCsId != null && targetStudentId != null) {
            boolean changedClassOrStudent = !targetStudentId.equals(existing.getStudentId())
                    || (existing.getCourseSectionId() == null || !targetCsId.equals(existing.getCourseSectionId()));
            if (changedClassOrStudent && repo.existsByStudentIdAndCourseSectionIdAndDeletedAtIsNull(targetStudentId, targetCsId)) {
                throw new RuntimeException("Sinh viên đã đăng ký lớp học phần này rồi!");
            }
        }

        if (updateData.getStudentId() != null) {
            existing.setStudentId(updateData.getStudentId());
            studentRepo.findById(updateData.getStudentId()).ifPresent(st -> {
                existing.setStudentName(st.getFullName());
                existing.setStudentCode(st.getStudentCode());
            });
        }
        if (updateData.getStudentCode() != null && !updateData.getStudentCode().isBlank()) existing.setStudentCode(updateData.getStudentCode());
        if (updateData.getStudentName() != null && !updateData.getStudentName().isBlank()) existing.setStudentName(updateData.getStudentName());
        if (updateData.getRegistrationDate() != null) existing.setRegistrationDate(updateData.getRegistrationDate());
        if (updateData.getRegistrationType() != null) existing.setRegistrationType(updateData.getRegistrationType());
        if (updateData.getStatus() != null) existing.setStatus(updateData.getStatus());
        if (updateData.getAttemptNumber() != null) existing.setAttemptNumber(updateData.getAttemptNumber());
        if (updateData.getNote() != null) existing.setNote(updateData.getNote());
        if (updateData.getIsActive() != null) existing.setIsActive(updateData.getIsActive());

        if (targetCsId != null) {
            CourseSection cs = csRepo.findById(targetCsId).orElse(null);
            if (cs != null) existing.setCourseSection(cs);
        }

        StudentCourseSection saved = repo.save(existing);
        enrichRegistrations(List.of(saved));
        return saved;
    }

    public StudentCourseSection toggleActive(UUID id, Boolean isActive) {
        StudentCourseSection existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký với ID: " + id));
        existing.setIsActive(isActive != null ? isActive : !existing.getIsActive());
        StudentCourseSection saved = repo.save(existing);
        enrichRegistrations(List.of(saved));
        return saved;
    }

    public void delete(UUID id) {
        StudentCourseSection existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký với ID: " + id));
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        repo.save(existing);
    }
}
