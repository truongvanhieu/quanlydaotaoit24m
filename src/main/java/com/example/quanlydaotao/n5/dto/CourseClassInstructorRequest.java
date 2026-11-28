package com.example.quanlydaotao.n5.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CourseClassInstructorRequest {

    private String instructorCode;
    private String instructorName;
    private UUID employeeId;
    private UUID courseClassId;
    private String role;
    private Integer teachingHours;
    private Boolean isActive;
}
