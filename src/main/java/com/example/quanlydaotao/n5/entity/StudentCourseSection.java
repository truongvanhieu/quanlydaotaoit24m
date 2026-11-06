package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_course_sections")
public class StudentCourseSection extends BaseEntity {
    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "student_code", length = 100)
    private String studentCode;

    @Column(name = "student_name", length = 255)
    private String studentName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_section_id")
    private CourseSection courseSection;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "registration_type", length = 100)
    private String registrationType = "Học lần đầu";

    @Column(name = "status", length = 100)
    private String status = "REGISTERED";

    @Column(name = "attempt_number", length = 100)
    private String attemptNumber = "1";

    @Column(name = "note", length = 255)
    private String note;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (this.registrationDate == null) {
            this.registrationDate = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "REGISTERED";
        }
        if (this.registrationType == null || this.registrationType.isBlank()) {
            this.registrationType = "Học lần đầu";
        }
        if (this.attemptNumber == null || this.attemptNumber.isBlank()) {
            this.attemptNumber = "1";
        }
    }

    // Getters & Setters
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public CourseSection getCourseSection() { return courseSection; }
    public void setCourseSection(CourseSection courseSection) { this.courseSection = courseSection; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public String getRegistrationType() { return registrationType; }
    public void setRegistrationType(String registrationType) { this.registrationType = registrationType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(String attemptNumber) { this.attemptNumber = attemptNumber; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
