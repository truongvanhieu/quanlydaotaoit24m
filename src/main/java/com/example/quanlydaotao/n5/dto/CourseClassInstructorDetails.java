package com.example.quanlydaotao.n5.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CourseClassInstructorDetails {

    UUID getId();

    UUID getEmployeeId();

    String getInstructorCode();

    String getInstructorName();

    UUID getCourseClassId();

    String getCourseSectionCode();

    String getCourseSectionName();

    String getRole();

    Integer getTeachingHours();

    LocalDateTime getCreatedAt();

    Boolean getIsActive();
}
