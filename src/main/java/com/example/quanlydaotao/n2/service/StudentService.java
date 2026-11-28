package com.example.quanlydaotao.n2.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.entity.StudentClasseSection;
import com.example.quanlydaotao.n2.entity.StudentStatus;
import com.example.quanlydaotao.n2.entity.StudentStatusHistory;
import com.example.quanlydaotao.n2.repository.StudentClasseSectionRepository;
import com.example.quanlydaotao.n2.repository.StudentRepository;
import com.example.quanlydaotao.n2.repository.StudentStatusHistoryRepository;
import com.example.quanlydaotao.n2.repository.StudentStatusRepository;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentStatusRepository studentStatusRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;
    private final StudentClasseSectionRepository studentClasseSectionRepository;

    public StudentService(
            StudentRepository studentRepository,
            StudentStatusRepository studentStatusRepository,
            StudentStatusHistoryRepository studentStatusHistoryRepository,
            StudentClasseSectionRepository studentClasseSectionRepository) {
        this.studentRepository = studentRepository;
        this.studentStatusRepository = studentStatusRepository;
        this.studentStatusHistoryRepository = studentStatusHistoryRepository;
        this.studentClasseSectionRepository = studentClasseSectionRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Student> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll();
        }
        return studentRepository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public Student getById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Student> getByClassId(UUID classId) {
        return studentRepository.findByStudentClassessId(classId);
    }

    public Student save(Student student) {
        boolean isNew = (student.getId() == null);
        String trimmedCode = student.getCode() == null ? "" : student.getCode().trim();
        if (isNew) {
            if (trimmedCode.isEmpty() || studentRepository.existsByCode(trimmedCode)) {
                student.setCode(generateUniqueStudentCode(student));
            } else {
                student.setCode(trimmedCode);
            }
        } else if (!trimmedCode.isEmpty()) {
            student.setCode(trimmedCode);
        }

        String newStatusCode = student.getStatus() == null ? null : student.getStatus().trim();
        String oldStatusCode = null;
        UUID oldClassId = null;

        if (!isNew) {
            Student existingStudent = studentRepository.findById(student.getId()).orElse(null);
            if (existingStudent != null) {
                oldStatusCode = existingStudent.getStatus() == null ? null : existingStudent.getStatus().trim();
                oldClassId = existingStudent.getStudentClassessId();
            }
        }

        StudentStatus newStatus = null;
        if (newStatusCode != null && !newStatusCode.isEmpty()) {
            newStatus = studentStatusRepository.findByCode(newStatusCode)
                    .filter(status -> status.getIsActive() != false)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạng thái sinh viên: " + newStatusCode));
            student.setStatus(newStatusCode);
        } else {
            student.setStatus(null);
        }

        Student saved = studentRepository.save(student);

        if (!java.util.Objects.equals(oldStatusCode, newStatusCode)) {
            if (student.getId() != null) {
                studentStatusHistoryRepository.findByStudentId(saved.getId()).stream()
                        .filter(history -> history.getIsActive() != false && history.getEndDate() == null)
                        .forEach(history -> {
                            history.setEndDate(LocalDate.now());
                            history.setUpdatedAt(LocalDateTime.now());
                            studentStatusHistoryRepository.save(history);
                        });
            }

            StudentStatusHistory history = new StudentStatusHistory();
            history.setStudentId(saved.getId());
            history.setStatusId(newStatus == null ? null : newStatus.getId());
            history.setStartDate(LocalDate.now());
            history.setReason(student.getId() == null ? "Khởi tạo trạng thái sinh viên" : "Thay đổi trạng thái sinh viên");
            history.setCreatedAt(LocalDateTime.now());
            history.setIsActive(true);
            studentStatusHistoryRepository.save(history);
        }

        UUID newClassId = saved.getStudentClassessId();
        if (isNew || !java.util.Objects.equals(oldClassId, newClassId)) {
            syncStudentClassSection(saved.getId(), newClassId, isNew);
        }

        return saved;
    }

    private void syncStudentClassSection(UUID studentId, UUID newClassId, boolean isNew) {
        LocalDateTime now = LocalDateTime.now();
        List<StudentClasseSection> existingSections = studentClasseSectionRepository.findByStudentId(studentId);
        boolean alreadyActiveInNewClass = false;

        for (StudentClasseSection section : existingSections) {
            if (section.getIsActive() != false) {
                if (newClassId != null && newClassId.equals(section.getStudentClasseId())) {
                    alreadyActiveInNewClass = true;
                } else {
                    section.setStatus(newClassId != null ? "transferred" : "dropped");
                    if (section.getEndDate() == null) {
                        section.setEndDate(now);
                    }
                    section.setIsActive(false);
                    section.setUpdatedAt(now);
                    studentClasseSectionRepository.save(section);
                }
            }
        }

        if (newClassId != null && !alreadyActiveInNewClass) {
            StudentClasseSection newSection = new StudentClasseSection();
            newSection.setStudentId(studentId);
            newSection.setStudentClasseId(newClassId);
            newSection.setStatus("studying");
            newSection.setStartDate(now);
            newSection.setNote(isNew ? "Phân lớp khi tạo hồ sơ sinh viên" : "Chuyển lớp từ trang Hồ sơ sinh viên");
            newSection.setCreatedAt(now);
            newSection.setIsActive(true);
            studentClasseSectionRepository.save(newSection);
        }
    }

    public void delete(UUID id) {
        // Soft delete
        Student student = getById(id);
        student.setIsActive(false);
        student.setDeletedAt(java.time.LocalDateTime.now());
        studentRepository.save(student);
    }

    public void hardDelete(UUID id) {
        studentRepository.deleteById(id);
    }

    private String generateUniqueStudentCode(Student student) {
        int year = (student.getAdmissionYear() != null)
                ? student.getAdmissionYear().getYear()
                : LocalDate.now().getYear();
        String prefix = String.format("SV%02d", year % 100);
        int maxSeq = 0;

        for (Student existing : studentRepository.findAll()) {
            String code = existing.getCode();
            if (code != null && code.toUpperCase().startsWith(prefix)) {
                String suffix = code.substring(prefix.length());
                if (suffix.matches("\\d+")) {
                    try {
                        int seq = Integer.parseInt(suffix);
                        if (seq > maxSeq) {
                            maxSeq = seq;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        int nextSeq = maxSeq + 1;
        String candidate = String.format("%s%03d", prefix, nextSeq);
        while (studentRepository.existsByCode(candidate)) {
            nextSeq++;
            candidate = String.format("%s%03d", prefix, nextSeq);
        }
        return candidate;
    }
}