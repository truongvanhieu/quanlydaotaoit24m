package com.example.quanlydaotao.n5.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CourseSectionStudent {
    UUID getRegistrationId();
    UUID getStudentId();

    String getCourseSectionCode();

    String getCourseSectionName();

    LocalDateTime getRegistrationDate();

    String getStatus();

    String getAttemptNumber();

    Boolean getIsActive();
}
