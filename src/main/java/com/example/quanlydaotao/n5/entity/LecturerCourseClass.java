package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lecturer_course_classes")
public class LecturerCourseClass extends BaseEntity {

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_class_id")
    private CourseSection courseSection;

    @Column(name = "course_class_name", length = 255)
    private String courseClassName;

    @Column(name = "role", length = 50)
    private String role = "Giảng viên chính";

    @Column(name = "note", length = 255)
    private String note;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (this.role == null || this.role.isBlank()) {
            this.role = "Giảng viên chính";
        }
    }

    // Getters & Setters
    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }

    // Aliases for compatibility
    public UUID getLecturerId() { return employeeId; }
    public void setLecturerId(UUID lecturerId) { this.employeeId = lecturerId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public CourseSection getCourseSection() { return courseSection; }
    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
        if (courseSection != null && (this.courseClassName == null || this.courseClassName.isBlank())) {
            this.courseClassName = courseSection.getName();
        }
    }

    public String getCourseClassName() { return courseClassName; }
    public void setCourseClassName(String courseClassName) { this.courseClassName = courseClassName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
