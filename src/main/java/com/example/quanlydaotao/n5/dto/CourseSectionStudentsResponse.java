package com.example.quanlydaotao.n5.dto;

import java.util.List;
import java.util.UUID;

public record CourseSectionStudentsResponse(
        UUID courseSectionId,
        String courseSectionCode,
        String courseSectionName,
        long totalStudents,
        List<CourseSectionStudent> students
) {
}
