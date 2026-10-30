package com.example.quanlydaotao.n5.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface StudentCourseSectionDetails {

    UUID getId();

    UUID getStudentId();

    String getStudentCode();

    String getStudentName();

    UUID getCourseSectionId();

    String getCourseSectionCode();

    String getCourseSectionName();

    LocalDateTime getRegistrationDate();

    String getRegistrationType();

    String getStatus();

    Boolean getIsActive();
}
