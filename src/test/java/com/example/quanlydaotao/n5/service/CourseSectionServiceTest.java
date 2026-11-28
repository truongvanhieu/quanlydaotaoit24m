package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.CourseSection;
import com.example.quanlydaotao.n5.repository.CourseClassInstructorRepository;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.StudentCourseSectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CourseSectionServiceTest {

    @Mock
    private CourseSectionRepository repository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private StudentCourseSectionRepository registrationRepository;

    @Mock
    private CourseClassInstructorRepository instructorRepository;

    @InjectMocks
    private CourseSectionService service;

    @Test
    void requiresFieldsPresentInTheDatabaseSchema() {
        CourseSection request = CourseSection.builder().name("Lớp học phần").build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void rejectsNegativeCapacity() {
        CourseSection request = validSection();
        request.setMaxStudents(-1);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void rejectsDuplicateSectionCode() {
        CourseSection request = validSection();
        when(semesterRepository.existsById(request.getSemesterId())).thenReturn(true);
        when(repository.existsByCode("CS-01")).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void createsSectionWithItsRequiredSemester() {
        CourseSection request = validSection();
        when(semesterRepository.existsById(request.getSemesterId())).thenReturn(true);
        when(repository.existsByCode("CS-01")).thenReturn(false);
        when(repository.save(any(CourseSection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseSection result = service.create(request);

        assertEquals("CS-01", result.getCode());
        assertEquals(request.getSemesterId(), result.getSemesterId());
        assertEquals("open", result.getStatus());
    }

    private CourseSection validSection() {
        return CourseSection.builder()
                .code("CS-01")
                .name("Lớp học phần")
                .semesterId(UUID.randomUUID())
                .build();
    }
}
