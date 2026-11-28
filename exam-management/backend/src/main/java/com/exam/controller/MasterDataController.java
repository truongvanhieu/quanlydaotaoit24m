package com.exam.controller;

import com.exam.dto.ApiResponse;
import com.exam.entity.Course;
import com.exam.entity.Semester;
import com.exam.entity.Room;
import com.exam.entity.Student;
import com.exam.repository.CourseRepository;
import com.exam.repository.SemesterRepository;
import com.exam.repository.RoomRepository;
import com.exam.repository.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Bổ sung bản ghi danh mục từ giao diện, không thay đổi các API sẵn có. */
@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {
    private final CourseRepository courses;
    private final SemesterRepository semesters;
    private final RoomRepository rooms;
    private final StudentRepository students;

    public MasterDataController(CourseRepository courses, SemesterRepository semesters,
                                RoomRepository rooms, StudentRepository students) {
        this.courses = courses;
        this.semesters = semesters;
        this.rooms = rooms;
        this.students = students;
    }

    private String value(Map<String, Object> body, String key, int max, boolean required) {
        Object raw = body.get(key);
        String value = raw == null ? "" : raw.toString().trim();
        if (required && value.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập " + key);
        if (value.length() > max) throw new IllegalArgumentException(key + " chỉ được tối đa " + max + " ký tự");
        return value;
    }

    private int integer(Map<String, Object> body, String key, int low, int high) {
        String input = value(body, key, 8, true);
        try {
            int number = Integer.parseInt(input);
            if (number < low || number > high) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " phải là số từ " + low + " đến " + high);
        }
    }

    private ResponseEntity<ApiResponse<Object>> result(String name, UUID id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id.toString());
        return ResponseEntity.status(201).body(ApiResponse.success("Đã lưu " + name + " vào SQL Server", data));
    }

    private ResponseEntity<ApiResponse<Object>> error(Exception e) {
        String message = e instanceof DataIntegrityViolationException
            ? "Mã này đã tồn tại hoặc dữ liệu không đáp ứng ràng buộc SQL Server"
            : e.getMessage();
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<Object>> createCourse(@RequestBody Map<String, Object> body) {
        try {
            Course entity = new Course();
            entity.setCourseCode(value(body, "course_code", 50, true));
            entity.setCourseName(value(body, "course_name", 200, true));
            entity.setCredits(integer(body, "credits", 1, 30));
            return result("môn học", courses.saveAndFlush(entity).getId());
        } catch (IllegalArgumentException | DataIntegrityViolationException e) { return error(e); }
    }

    @PostMapping("/semesters")
    public ResponseEntity<ApiResponse<Object>> createSemester(@RequestBody Map<String, Object> body) {
        try {
            Semester entity = new Semester();
            entity.setSemesterCode(value(body, "semester_code", 50, true));
            entity.setSemesterName(value(body, "semester_name", 100, true));
            entity.setAcademicYear(value(body, "academic_year", 20, true));
            return result("học kỳ", semesters.saveAndFlush(entity).getId());
        } catch (IllegalArgumentException | DataIntegrityViolationException e) { return error(e); }
    }

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<Object>> createRoom(@RequestBody Map<String, Object> body) {
        try {
            Room entity = new Room();
            entity.setRoomNumber(value(body, "room_number", 50, true));
            entity.setBuilding(value(body, "building", 100, true));
            entity.setCapacity(integer(body, "capacity", 1, 10000));
            return result("phòng vật lý", rooms.saveAndFlush(entity).getId());
        } catch (IllegalArgumentException | DataIntegrityViolationException e) { return error(e); }
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<Object>> createStudent(@RequestBody Map<String, Object> body) {
        try {
            Student entity = new Student();
            entity.setStudentCode(value(body, "student_code", 20, true));
            entity.setFullName(value(body, "full_name", 150, true));
            String email = value(body, "email", 100, false);
            if (!email.isEmpty() && (!email.contains("@") || email.indexOf('@') == 0)) {
                throw new IllegalArgumentException("Email không hợp lệ");
            }
            entity.setEmail(email.isEmpty() ? null : email);
            String phone = value(body, "phone", 20, false);
            entity.setPhone(phone.isEmpty() ? null : phone);
            String clazz = value(body, "class_name", 50, false);
            entity.setClassName(clazz.isEmpty() ? null : clazz);
            String birthDate = value(body, "birth_date", 10, false);
            if (!birthDate.isEmpty()) {
                try { entity.setBirthDate(java.time.LocalDate.parse(birthDate)); }
                catch (java.time.DateTimeException ex) { throw new IllegalArgumentException("Ngày sinh không hợp lệ"); }
            }
            return result("sinh viên", students.saveAndFlush(entity).getId());
        } catch (IllegalArgumentException | DataIntegrityViolationException e) { return error(e); }
    }
}
