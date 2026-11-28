package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ExamPaperResponseDto {
    private UUID id;
    @JsonProperty("exam_id")
    private UUID examId;
    @JsonProperty("course_code")
    private String courseCode;
    @JsonProperty("course_name")
    private String courseName;
    @JsonProperty("exam_date")
    private String examDate;
    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("paper_code")
    private String paperCode;
    @JsonProperty("file_url")
    private String fileUrl;
    @JsonProperty("created_at")
    private String createdAt;

    public ExamPaperResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getExamId() { return examId; }
    public void setExamId(UUID examId) { this.examId = examId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getPaperCode() { return paperCode; }
    public void setPaperCode(String paperCode) { this.paperCode = paperCode; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
