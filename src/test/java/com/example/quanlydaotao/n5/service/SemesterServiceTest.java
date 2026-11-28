package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.entity.Semester;
import com.example.quanlydaotao.n5.repository.CourseSectionRepository;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock
    private SemesterRepository repository;

    @Mock
    private SchoolYearRepository schoolYearRepository;

    @Mock
    private CourseSectionRepository courseSectionRepository;

    @InjectMocks
    private SemesterService service;

    @Test
    void requiresSchoolYearWhenCreatingSemester() {
        Semester request = Semester.builder().code("HK1").name("Học kỳ mùa thu").build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void rejectsSemesterForUnknownSchoolYear() {
        UUID schoolYearId = UUID.randomUUID();
        Semester request = Semester.builder()
                .code("HK1")
                .name("Học kỳ mùa thu")
                .schoolYearId(schoolYearId)
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void createsSemesterUsingTheActualSchoolYearRelation() {
        UUID schoolYearId = UUID.randomUUID();
        UUID semesterId = UUID.randomUUID();
        Semester request = Semester.builder()
                .id(semesterId)
                .code("HK1")
                .name("Học kỳ mùa thu")
                .schoolYearId(schoolYearId)
                .build();
        SchoolYear schoolYear = SchoolYear.builder()
                .id(schoolYearId)
                .name("Năm học 2026-2027")
                .build();
        when(schoolYearRepository.existsById(schoolYearId)).thenReturn(true);
        when(schoolYearRepository.findById(schoolYearId)).thenReturn(Optional.of(schoolYear));
        when(repository.save(any(Semester.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Semester result = service.create(request);

        assertEquals(schoolYearId, result.getSchoolYearId());
        verify(repository).save(request);
    }
}
