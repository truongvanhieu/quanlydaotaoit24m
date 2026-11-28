package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.entity.*;
import com.exam.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LookupService {

    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final ExamTypeRepository examTypeRepository;
    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamRegistrationRepository examRegistrationRepository;

    public LookupService(
            SemesterRepository semesterRepository,
            CourseRepository courseRepository,
            RoomRepository roomRepository,
            StudentRepository studentRepository,
            ExamTypeRepository examTypeRepository,
            ExamRepository examRepository,
            ExamRoomRepository examRoomRepository,
            ExamRegistrationRepository examRegistrationRepository) {
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.examTypeRepository = examTypeRepository;
        this.examRepository = examRepository;
        this.examRoomRepository = examRoomRepository;
        this.examRegistrationRepository = examRegistrationRepository;
    }

    public ApiResponse<Object> getAllLookups() {
        Map<String, Object> result = new HashMap<>();

        // Semesters
        List<Map<String, Object>> semesterList = new ArrayList<>();
        for (Semester s : semesterRepository.findByIsActiveTrueOrderBySemesterCodeDesc()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("semester_code", s.getSemesterCode());
            m.put("semester_name", s.getSemesterName());
            m.put("academic_year", s.getAcademicYear());
            semesterList.add(m);
        }
        result.put("semesters", semesterList);

        // Courses
        List<Map<String, Object>> courseList = new ArrayList<>();
        for (Course c : courseRepository.findByIsActiveTrueOrderByCourseCodeAsc()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("course_code", c.getCourseCode());
            m.put("course_name", c.getCourseName());
            m.put("credits", c.getCredits());
            courseList.add(m);
        }
        result.put("courses", courseList);

        // Physical Rooms
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (Room r : roomRepository.findByIsActiveTrueOrderByRoomNumberAsc()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("room_number", r.getRoomNumber());
            m.put("building", r.getBuilding());
            m.put("capacity", r.getCapacity());
            roomList.add(m);
        }
        result.put("rooms", roomList);

        // Students
        List<Map<String, Object>> studentList = new ArrayList<>();
        for (Student st : studentRepository.findByIsActiveTrueOrderByStudentCodeAsc()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", st.getId());
            m.put("student_code", st.getStudentCode());
            m.put("full_name", st.getFullName());
            m.put("email", st.getEmail());
            m.put("phone", st.getPhone());
            m.put("class_name", st.getClassName());
            m.put("birth_date", st.getBirthDate() == null ? null : st.getBirthDate().toString());
            studentList.add(m);
        }
        result.put("students", studentList);

        // Exam Types
        List<Map<String, Object>> examTypeList = new ArrayList<>();
        for (ExamType et : examTypeRepository.findAllActive()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", et.getId());
            m.put("name", et.getName());
            m.put("description", et.getDescription());
            examTypeList.add(m);
        }
        result.put("examTypes", examTypeList);

        // Active Exams
        List<Map<String, Object>> examList = new ArrayList<>();
        for (Exam ex : examRepository.findAllActive()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", ex.getId());
            m.put("exam_type_id", ex.getExamTypeId());
            m.put("exam_type_name", ex.getExamType() != null ? ex.getExamType().getName() : "");
            m.put("course_class_id", ex.getCourseClassId());
            m.put("course_code", ex.getCourse() != null ? ex.getCourse().getCourseCode() : "");
            m.put("course_name", ex.getCourse() != null ? ex.getCourse().getCourseName() : "");
            m.put("semester_id", ex.getSemesterId());
            m.put("exam_date", ex.getExamDate() != null ? ex.getExamDate().toString() : "");
            m.put("start_time", ex.getStartTime() != null ? ex.getStartTime().toString() : "");
            m.put("end_time", ex.getEndTime() != null ? ex.getEndTime().toString() : "");
            m.put("exam_format", ex.getExamFormat());
            m.put("exam_status", ex.getExamStatus());
            examList.add(m);
        }
        result.put("exams", examList);

        // Exam Rooms
        List<Map<String, Object>> examRoomList = new ArrayList<>();
        for (ExamRoom er : examRoomRepository.findAllActive()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", er.getId());
            m.put("exam_id", er.getExamId());
            m.put("room_id", er.getRoomId());
            m.put("room_number", er.getRoom() != null ? er.getRoom().getRoomNumber() : "");
            m.put("building", er.getRoom() != null ? er.getRoom().getBuilding() : "");
            m.put("capacity", er.getCapacity());
            m.put("current_registrations", examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(er.getId()));
            examRoomList.add(m);
        }
        result.put("examRooms", examRoomList);

        return ApiResponse.successData(result);
    }
}
