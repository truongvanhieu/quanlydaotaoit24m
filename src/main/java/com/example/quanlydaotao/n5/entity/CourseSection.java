package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "semester_id")
    private UUID semesterId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "building_id")
    private UUID buildingId;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "min_students")
    private Integer minStudents;

    @Column(name = "class_type", length = 255)
    private String classType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "registration_start")
    private LocalDateTime registrationStart;

    @Column(name = "registration_end")
    private LocalDateTime registrationEnd;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
