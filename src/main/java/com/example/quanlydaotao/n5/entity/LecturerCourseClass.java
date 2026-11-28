package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.util.UUID;

@Entity
@Table(name = "lecturer_course_classes")
public class LecturerCourseClass extends BaseEntity {

    @Column(name = "lecturer_id")
    private UUID lecturerId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Nationalized
    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_class_id")
    private CourseSection courseSection;

    @Transient
    private UUID transientCourseClassId;

    @Nationalized
    @Column(name = "course_class_name", length = 255)
    private String courseClassName;

    @Nationalized
    @Column(name = "role", length = 100)
    private String role = "Giảng viên chính";

    @Column(name = "hours")
    private Integer hours = 45;

    @Nationalized
    @Column(name = "note", length = 255)
    private String note;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (this.role == null || this.role.isBlank()) {
            this.role = "Giảng viên chính";
        }
        if (this.hours == null || this.hours <= 0) {
            this.hours = 45;
        }
        if (this.lecturerId == null && this.employeeId != null) {
            this.lecturerId = this.employeeId;
        } else if (this.employeeId == null && this.lecturerId != null) {
            this.employeeId = this.lecturerId;
        }
    }

    public UUID getEmployeeId() {
        return employeeId != null ? employeeId : lecturerId;
    }
    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
        if (employeeId != null) this.lecturerId = employeeId;
    }

    public UUID getLecturerId() {
        return lecturerId != null ? lecturerId : employeeId;
    }
    public void setLecturerId(UUID lecturerId) {
        this.lecturerId = lecturerId;
        if (lecturerId != null) this.employeeId = lecturerId;
    }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getLecturerName() { return employeeName; }
    public void setLecturerName(String lecturerName) { this.employeeName = lecturerName; }

    public CourseSection getCourseSection() { return courseSection; }
    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
        if (courseSection != null) {
            this.transientCourseClassId = courseSection.getId();
            if (this.courseClassName == null || this.courseClassName.isBlank()) {
                this.courseClassName = courseSection.getName();
            }
        }
    }

    public UUID getCourseClassId() {
        if (courseSection != null && courseSection.getId() != null) {
            return courseSection.getId();
        }
        return transientCourseClassId;
    }
    public void setCourseClassId(UUID courseClassId) {
        this.transientCourseClassId = courseClassId;
    }

    public UUID getCourseSectionId() { return getCourseClassId(); }
    public void setCourseSectionId(UUID courseSectionId) { setCourseClassId(courseSectionId); }

    public String getCourseClassName() {
        if (courseClassName != null && !courseClassName.isBlank()) {
            return courseClassName;
        }
        return courseSection != null ? courseSection.getName() : null;
    }
    public void setCourseClassName(String courseClassName) { this.courseClassName = courseClassName; }

    public String getCourseSectionCode() {
        return courseSection != null ? courseSection.getCode() : null;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getHours() { return hours != null ? hours : 45; }
    public void setHours(Integer hours) { this.hours = hours; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
