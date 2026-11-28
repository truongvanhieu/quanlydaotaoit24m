package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_sections")
public class CourseSection extends BaseEntity {
    @Column(name = "code", length = 100)
    private String code;

    @Nationalized
    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "course_id")
    private UUID courseId;

    @Nationalized
    @Column(name = "course_name", length = 255)
    private String courseName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @Transient
    private UUID transientSemesterId;

    @Column(name = "lecturer_id")
    private UUID lecturerId;

    @Nationalized
    @Column(name = "lecturer_name", length = 255)
    private String lecturerName;

    @Column(name = "room_id")
    private UUID roomId;

    @Nationalized
    @Column(name = "room_name", length = 255)
    private String roomName;

    @Column(name = "building_id")
    private UUID buildingId;

    @Column(name = "max_students")
    private Integer maxStudents = 50;

    @Column(name = "min_students")
    private Integer minStudents = 15;

    @Nationalized
    @Column(name = "class_type", length = 255)
    private String classType = "Lý thuyết";

    @Nationalized
    @Column(name = "status", length = 100)
    private String status = "Đang mở";

    @Column(name = "registration_start")
    private LocalDateTime registrationStart;

    @Column(name = "registration_end")
    private LocalDateTime registrationEnd;

    @Nationalized
    @Column(name = "note", length = 255)
    private String note;

    @Transient
    private Long currentStudents = 0L;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) {
        this.semester = semester;
        if (semester != null) {
            this.transientSemesterId = semester.getId();
        }
    }

    public UUID getSemesterId() {
        if (semester != null && semester.getId() != null) {
            return semester.getId();
        }
        return transientSemesterId;
    }

    public void setSemesterId(UUID semesterId) {
        this.transientSemesterId = semesterId;
    }

    public String getSemesterName() {
        return semester != null ? semester.getName() : null;
    }

    public String getSemesterShortName() {
        if (semester == null) return "";
        String semName = semester.getName() != null ? semester.getName().trim() : "";
        String syName = semester.getSchoolYearName() != null ? semester.getSchoolYearName().trim() : "";
        String lower = semName.toLowerCase();
        String hk = semName;
        if (lower.startsWith("học kỳ 1") || lower.equals("hk1")) {
            hk = "HK1";
        } else if (lower.startsWith("học kỳ 2") || lower.equals("hk2")) {
            hk = "HK2";
        } else if (lower.startsWith("học kỳ hè") || lower.startsWith("học kỳ 3") || lower.equals("hk hè")) {
            hk = "HK Hè";
        }
        if (!syName.isEmpty() && !hk.contains(syName)) {
            return hk + " " + syName;
        }
        return hk;
    }

    public String getDisplayName() {
        String codeStr = (code != null && !code.isBlank()) ? code.trim() : "LHP";
        String rawCourse = (courseName != null && !courseName.isBlank()) ? courseName.trim() : "";
        if (rawCourse.contains(" - ")) {
            rawCourse = rawCourse.substring(rawCourse.indexOf(" - ") + 3).trim();
        }
        String secName = (name != null && !name.isBlank()) ? name.trim() : "";
        String body;
        if (!rawCourse.isEmpty() && !secName.toLowerCase().contains(rawCourse.toLowerCase())) {
            body = rawCourse + (secName.isEmpty() ? "" : " - " + secName);
        } else {
            body = !secName.isEmpty() ? secName : rawCourse;
        }
        String semShort = getSemesterShortName();
        return "[" + codeStr + "] - " + body + (!semShort.isEmpty() ? " (" + semShort + ")" : "");
    }

    public UUID getLecturerId() { return lecturerId; }
    public void setLecturerId(UUID lecturerId) { this.lecturerId = lecturerId; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

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

    public Long getCurrentStudents() { return currentStudents != null ? currentStudents : 0L; }
    public void setCurrentStudents(Long currentStudents) { this.currentStudents = currentStudents; }
}
