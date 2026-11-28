package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;
import java.util.Map;

public class ExamResponseDto {
    private UUID id;
    @JsonProperty("exam_type_id")
    private UUID examTypeId;
    @JsonProperty("exam_type_name")
    private String examTypeName;

    @JsonProperty("course_class_id")
    private UUID courseClassId;
    @JsonProperty("course_code")
    private String courseCode;
    @JsonProperty("course_name")
    private String courseName;

    @JsonProperty("semester_id")
    private UUID semesterId;
    @JsonProperty("semester_name")
    private String semesterName;
    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("exam_date")
    private String examDate;
    @JsonProperty("start_time")
    private String startTime;
    @JsonProperty("duration_minutes")
    private Short durationMinutes;
    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("exam_format")
    private String examFormat;
    @JsonProperty("exam_status")
    private String examStatus;
    @JsonProperty("supervisor_count")
    private Short supervisorCount;

    @JsonProperty("invigilator_ids")
    private List<UUID> invigilatorIds;
    @JsonProperty("invigilators")
    private List<Map<String, Object>> invigilators;

    public List<UUID> getInvigilatorIds() { return invigilatorIds; }
    public void setInvigilatorIds(List<UUID> value) { this.invigilatorIds = value; }
    public List<Map<String, Object>> getInvigilators() { return invigilators; }
    public void setInvigilators(List<Map<String, Object>> value) { this.invigilators = value; }

    @JsonProperty("room_count")
    private long roomCount;
    @JsonProperty("registration_count")
    private long registrationCount;

    public ExamResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getExamTypeId() { return examTypeId; }
    public void setExamTypeId(UUID examTypeId) { this.examTypeId = examTypeId; }

    public String getExamTypeName() { return examTypeName; }
    public void setExamTypeName(String examTypeName) { this.examTypeName = examTypeName; }

    public UUID getCourseClassId() { return courseClassId; }
    public void setCourseClassId(UUID courseClassId) { this.courseClassId = courseClassId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public UUID getSemesterId() { return semesterId; }
    public void setSemesterId(UUID semesterId) { this.semesterId = semesterId; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public Short getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Short durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getExamFormat() { return examFormat; }
    public void setExamFormat(String examFormat) { this.examFormat = examFormat; }

    public String getExamStatus() { return examStatus; }
    public void setExamStatus(String examStatus) { this.examStatus = examStatus; }

    public Short getSupervisorCount() { return supervisorCount; }
    public void setSupervisorCount(Short supervisorCount) { this.supervisorCount = supervisorCount; }

    public long getRoomCount() { return roomCount; }
    public void setRoomCount(long roomCount) { this.roomCount = roomCount; }

    public long getRegistrationCount() { return registrationCount; }
    public void setRegistrationCount(long registrationCount) { this.registrationCount = registrationCount; }
}
