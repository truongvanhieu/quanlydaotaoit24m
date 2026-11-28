package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import java.util.List;

public class ExamRequestDto {
    @JsonProperty("exam_type_id")
    private UUID examTypeId;

    @JsonProperty("course_class_id")
    private UUID courseClassId;

    @JsonProperty("semester_id")
    private UUID semesterId;

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

    public List<UUID> getInvigilatorIds() { return invigilatorIds; }
    public void setInvigilatorIds(List<UUID> value) { this.invigilatorIds = value; }

    public ExamRequestDto() {}

    public UUID getExamTypeId() { return examTypeId; }
    public void setExamTypeId(UUID examTypeId) { this.examTypeId = examTypeId; }

    public UUID getCourseClassId() { return courseClassId; }
    public void setCourseClassId(UUID courseClassId) { this.courseClassId = courseClassId; }

    public UUID getSemesterId() { return semesterId; }
    public void setSemesterId(UUID semesterId) { this.semesterId = semesterId; }

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
}
