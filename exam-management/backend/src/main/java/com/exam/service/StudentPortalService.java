package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.entity.*;
import com.exam.repository.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StudentPortalService {

    private final StudentRepository studentRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamPaperRepository examPaperRepository;

    public StudentPortalService(
            StudentRepository studentRepository,
            ExamRegistrationRepository examRegistrationRepository,
            ExamResultRepository examResultRepository,
            ExamPaperRepository examPaperRepository) {
        this.studentRepository = studentRepository;
        this.examRegistrationRepository = examRegistrationRepository;
        this.examResultRepository = examResultRepository;
        this.examPaperRepository = examPaperRepository;
    }

    public ApiResponse<Object> getStudentScheduleAndResults(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return ApiResponse.error("Vui lòng cung cấp mã sinh viên hoặc ID sinh viên");
        }

        String clean = identifier.trim();
        Optional<Student> studentOpt = Optional.empty();

        // Check if identifier is UUID
        try {
            UUID uuid = UUID.fromString(clean);
            studentOpt = studentRepository.findById(uuid);
        } catch (IllegalArgumentException e) {
            studentOpt = studentRepository.findByStudentCodeAndIsActiveTrue(clean);
        }

        if (studentOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên có mã/ID: " + clean);
        }

        Student student = studentOpt.get();

        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("id", student.getId());
        studentInfo.put("student_code", student.getStudentCode());
        studentInfo.put("full_name", student.getFullName());
        studentInfo.put("email", student.getEmail());
        studentInfo.put("phone", student.getPhone());
        studentInfo.put("class_name", student.getClassName());

        // Registrations for this student
        List<ExamRegistration> registrations = examRegistrationRepository.findByStudentIdActive(student.getId());
        List<Map<String, Object>> examsList = new ArrayList<>();

        for (ExamRegistration reg : registrations) {
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("registration_id", reg.getId());
            examMap.put("roll_number", reg.getRollNumber());

            Exam exam = reg.getExam();
            if (exam != null) {
                examMap.put("exam_id", exam.getId());
                examMap.put("exam_type_name", exam.getExamType() != null ? exam.getExamType().getName() : "");
                if (exam.getCourse() != null) {
                    examMap.put("course_code", exam.getCourse().getCourseCode());
                    examMap.put("course_name", exam.getCourse().getCourseName());
                    examMap.put("credits", exam.getCourse().getCredits());
                }
                if (exam.getSemester() != null) {
                    examMap.put("semester_name", exam.getSemester().getSemesterName());
                    examMap.put("academic_year", exam.getSemester().getAcademicYear());
                }
                examMap.put("exam_date", exam.getExamDate() != null ? exam.getExamDate().toString() : "");
                examMap.put("start_time", exam.getStartTime() != null ? exam.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                examMap.put("duration_minutes", exam.getDurationMinutes());
                examMap.put("end_time", exam.getEndTime() != null ? exam.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                examMap.put("exam_format", exam.getExamFormat());
                examMap.put("exam_status", exam.getExamStatus());

                // Find paper if available
                List<ExamPaper> papers = examPaperRepository.findByExamIdActive(exam.getId());
                if (!papers.isEmpty()) {
                    examMap.put("paper_code", papers.get(0).getPaperCode());
                    examMap.put("paper_file_url", papers.get(0).getFileUrl());
                }
            }

            ExamRoom room = reg.getExamRoom();
            if (room != null) {
                examMap.put("exam_room_id", room.getId());
                examMap.put("room_capacity", room.getCapacity());
                if (room.getRoom() != null) {
                    examMap.put("room_number", room.getRoom().getRoomNumber());
                    examMap.put("building", room.getRoom().getBuilding());
                }
            }

            // Results
            Optional<ExamResult> resultOpt = examResultRepository.findByRegistrationIdActive(reg.getId());
            if (resultOpt.isPresent()) {
                ExamResult res = resultOpt.get();
                examMap.put("result_id", res.getId());
                examMap.put("score", res.getScore());
                examMap.put("result_status", res.getStatus());
                if (res.getGradedAt() != null) {
                    examMap.put("graded_at", res.getGradedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                examMap.put("is_locked", res.getIsLocked());
                examMap.put("appeal_status", res.getAppealStatus());
            }

            examsList.add(examMap);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("student", studentInfo);
        data.put("exams", examsList);

        return ApiResponse.successData(data);
    }
}
