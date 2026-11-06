package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_sections")
public class CourseSection extends BaseEntity {
    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "course_id")
    private UUID courseId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "building_id")
    private UUID buildingId;

    @Column(name = "max_students")
    private Integer maxStudents = 50;

    @Column(name = "min_students")
    private Integer minStudents = 15;

    @Column(name = "class_type", length = 255)
    private String classType;

    @Column(name = "status", length = 50)
    private String status = "open";

    @Column(name = "registration_start")
    private LocalDateTime registrationStart;

    @Column(name = "registration_end")
    private LocalDateTime registrationEnd;

    @Column(name = "note", length = 255)
    private String note;

    // Getters & Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }

    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public UUID getBuildingId() { return buildingId; }
    public void setBuildingId(UUID buildingId) { this.buildingId = buildingId; }

    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }

    public Integer getMinStudents() { return minStudents; }
    public void setMinStudents(Integer minStudents) { this.minStudents = minStudents; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(LocalDateTime registrationStart) { this.registrationStart = registrationStart; }

    public LocalDateTime getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(LocalDateTime registrationEnd) { this.registrationEnd = registrationEnd; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
