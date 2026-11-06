package com.example.quanlydaotao;

import com.example.quanlydaotao.n5.entity.*;
import com.example.quanlydaotao.n5.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class N5ServiceTests {

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private CourseSectionService courseSectionService;

    @Autowired
    private StudentCourseSectionService studentCourseSectionService;

    @Autowired
    private LecturerCourseClassService lecturerCourseClassService;

    @Autowired
    private StudentService studentService;

    @Test
    void testAcademicYearCRUD() {
        AcademicYear ay = new AcademicYear();
        ay.setCode("2024-2028");
        ay.setName("Khóa 2024-2028");
        ay.setYear("K24");
        ay.setStartDate(LocalDate.of(2024, 9, 1));
        ay.setEndDate(LocalDate.of(2028, 6, 30));

        AcademicYear saved = academicYearService.save(ay);
        assertNotNull(saved.getId());
        assertEquals("2024-2028", saved.getCode());

        List<AcademicYear> list = academicYearService.findAll(null);
        assertTrue(list.stream().anyMatch(i -> i.getCode().equals("2024-2028")));

        saved.setName("Khóa 2024-2028 Đã Cập Nhật");
        AcademicYear updated = academicYearService.update(saved.getId(), saved);
        assertEquals("Khóa 2024-2028 Đã Cập Nhật", updated.getName());

        academicYearService.toggleActive(saved.getId(), false);
        assertFalse(academicYearService.findById(saved.getId()).getIsActive());

        academicYearService.delete(saved.getId());
        assertFalse(academicYearService.findAll(null).stream().anyMatch(i -> i.getId().equals(saved.getId())));
    }

    @Test
    void testSchoolYearAndSemesterRelationship() {
        SchoolYear sy = new SchoolYear();
        sy.setCode("2025-2026");
        sy.setName("Năm học 2025-2026");
        SchoolYear savedSy = schoolYearService.save(sy);
        assertNotNull(savedSy.getId());

        Semester sem = new Semester();
        sem.setCode("HK1_2025_2026");
        sem.setName("Học kỳ 1 năm 2025-2026");
        Semester savedSem = semesterService.save(sem, savedSy.getId());

        assertNotNull(savedSem.getId());
        assertNotNull(savedSem.getSchoolYear());
        assertEquals(savedSy.getId(), savedSem.getSchoolYear().getId());
    }

    @Test
    void testCourseSectionAndRegistrationFlow() {
        // 1. Tạo Năm học & Học kỳ
        SchoolYear sy = new SchoolYear();
        sy.setCode("2024-2025");
        sy.setName("Năm học 2024-2025");
        sy = schoolYearService.save(sy);

        Semester sem = new Semester();
        sem.setCode("HK1_2024_2025");
        sem.setName("Học kỳ 1");
        sem = semesterService.save(sem, sy.getId());

        // 2. Tạo Lớp học phần
        CourseSection cs = new CourseSection();
        cs.setCode("IT101-01");
        cs.setName("Lập trình Java");
        cs.setMaxStudents(30);
        cs.setStatus("open");
        cs = courseSectionService.save(cs, sem.getId());
        assertNotNull(cs.getId());

        // 3. Tạo Sinh viên
        Student st = new Student();
        st.setStudentCode("SV24001");
        st.setFullName("Trần Văn Test");
        st.setEmail("test@donga.edu.vn");
        st = studentService.save(st);
        assertNotNull(st.getId());

        // 4. Đăng ký học phần
        StudentCourseSection reg = new StudentCourseSection();
        reg.setStudentId(st.getId());
        reg.setRegistrationType("Học lần đầu");
        StudentCourseSection savedReg = studentCourseSectionService.save(reg, cs.getId());

        assertNotNull(savedReg.getId());
        assertEquals(st.getId(), savedReg.getStudentId());
        assertEquals(cs.getId(), savedReg.getCourseSection().getId());
        assertEquals("Trần Văn Test", savedReg.getStudentName());

        // 5. Thử đăng ký trùng -> phải throw exception
        final UUID targetCsId = cs.getId();
        StudentCourseSection duplicateReg = new StudentCourseSection();
        duplicateReg.setStudentId(st.getId());
        assertThrows(RuntimeException.class, () -> studentCourseSectionService.save(duplicateReg, targetCsId));

        // 6. Phân công giảng viên
        LecturerCourseClass assign = new LecturerCourseClass();
        assign.setEmployeeName("TS. Nguyễn Văn A");
        assign.setRole("Giảng viên chính");
        LecturerCourseClass savedAssign = lecturerCourseClassService.save(assign, cs.getId());

        assertNotNull(savedAssign.getId());
        assertEquals("Lập trình Java", savedAssign.getCourseClassName());
    }
}
