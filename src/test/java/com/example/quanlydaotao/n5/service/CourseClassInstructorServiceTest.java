package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.dto.CourseClassInstructorRequest;
import com.example.quanlydaotao.n5.repository.CourseClassInstructorRepository;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.InstructorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseClassInstructorServiceTest {

    @Mock
    private CourseClassInstructorRepository repository;

    @Mock
    private CourseSectionRepository courseSectionRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @InjectMocks
    private CourseClassInstructorService service;

    @Test
    void requiresInstructorNameAndCodeTogether() {
        UUID courseClassId = UUID.randomUUID();
        CourseClassInstructorRequest request = new CourseClassInstructorRequest();
        request.setCourseClassId(courseClassId);
        request.setInstructorCode("ECO-005");
        when(courseSectionRepository.existsById(courseClassId)).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void rejectsNegativeTeachingHours() {
        UUID courseClassId = UUID.randomUUID();
        CourseClassInstructorRequest request = new CourseClassInstructorRequest();
        request.setCourseClassId(courseClassId);
        request.setInstructorCode("ECO-005");
        request.setInstructorName("Nguyễn An");
        request.setTeachingHours(-1);
        when(courseSectionRepository.existsById(courseClassId)).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, error.getStatusCode().value());
    }
}
