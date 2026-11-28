package com.example.quanlydaotao.n5.service;

import com.example.quanlydaotao.n5.entity.SchoolYear;
import com.example.quanlydaotao.n5.repository.SemesterRepository;
import com.example.quanlydaotao.n5.repository.SchoolYearRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolYearServiceTest {

    @Mock
    private SchoolYearRepository repository;

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private SchoolYearService service;

    @Test
    void returnsSchoolYearsInAscendingYearOrder() {
        when(repository.search(null, null)).thenReturn(List.of(
                SchoolYear.builder().id(UUID.randomUUID()).name("Năm học 2026 - 2027").build(),
                SchoolYear.builder().id(UUID.randomUUID()).name("Năm học 2000 - 2001").build(),
                SchoolYear.builder().id(UUID.randomUUID()).name("Năm học 2016 - 2017").build()
        ));

        List<SchoolYear> result = service.getAll(null, null);

        assertEquals(
                List.of("Năm học 2000 - 2001", "Năm học 2016 - 2017", "Năm học 2026 - 2027"),
                result.stream().map(SchoolYear::getName).toList());
    }

    @Test
    void updatesActiveStatusWithoutChangingDeletionState() {
        UUID id = UUID.randomUUID();
        SchoolYear schoolYear = SchoolYear.builder()
                .id(id)
                .name("Năm học 2026-2027")
                .isActive(true)
                .build();
        when(repository.findById(id)).thenReturn(java.util.Optional.of(schoolYear));
        when(repository.save(any(SchoolYear.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(id, false);

        assertFalse(schoolYear.getIsActive());
        verify(repository).save(schoolYear);
    }

    @Test
    void rejectsStatusUpdateWithoutAStatus() {
        ResponseStatusException error = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> service.updateStatus(UUID.randomUUID(), null));

        assertEquals(400, error.getStatusCode().value());
    }
}
