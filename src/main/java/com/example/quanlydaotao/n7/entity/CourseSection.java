package com.example.quanlydaotao.n7.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_sections")
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String code;

    @Column(length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "is_active")
    private Boolean active;

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

    @Column(name = "class_type")
    private String classType;

    @Column(name = "status")
    private String status;

    @Column(name = "registration_start")
    private LocalDateTime registrationStart;

    @Column(name = "registration_end")
    private LocalDateTime registrationEnd;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Course getCourse() {
        return course;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public UUID getSemesterId() { return semesterId; }
    public UUID getRoomId() { return roomId; }
    public UUID getBuildingId() { return buildingId; }
    public Integer getMaxStudents() { return maxStudents; }
    public Integer getMinStudents() { return minStudents; }
    public String getClassType() { return classType; }
    public String getStatus() { return status; }
    public LocalDateTime getRegistrationStart() { return registrationStart; }
    public LocalDateTime getRegistrationEnd() { return registrationEnd; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setCourse(Course course) { this.course = course; }
    public void setActive(Boolean active) { this.active = active; }
    public void setSemesterId(UUID semesterId) { this.semesterId = semesterId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }
    public void setBuildingId(UUID buildingId) { this.buildingId = buildingId; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public void setMinStudents(Integer minStudents) { this.minStudents = minStudents; }
    public void setClassType(String classType) { this.classType = classType; }
    public void setStatus(String status) { this.status = status; }
    public void setRegistrationStart(LocalDateTime value) { this.registrationStart = value; }
    public void setRegistrationEnd(LocalDateTime value) { this.registrationEnd = value; }
    public void setNote(String note) { this.note = note; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
