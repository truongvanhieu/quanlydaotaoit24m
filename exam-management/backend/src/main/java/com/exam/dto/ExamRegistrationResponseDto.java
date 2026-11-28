package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class ExamRegistrationResponseDto {
    private UUID id;
    @JsonProperty("merged_group_id")
    private UUID mergedGroupId;
    public UUID getMergedGroupId() { return mergedGroupId; }
    public void setMergedGroupId(UUID id) { this.mergedGroupId = id; }
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

    @JsonProperty("exam_room_id")
    private UUID examRoomId;
    @JsonProperty("room_number")
    private String roomNumber;
    @JsonProperty("building")
    private String building;
    @JsonProperty("room_capacity")
    private Integer roomCapacity;

    @JsonProperty("student_id")
    private UUID studentId;
    @JsonProperty("student_code")
    private String studentCode;
    @JsonProperty("student_name")
    private String studentName;
    @JsonProperty("student_class")
    private String studentClass;

    @JsonProperty("roll_number")
    private String rollNumber;

    @JsonProperty("result_id")
    private UUID resultId;
    @JsonProperty("score")
    private BigDecimal score;
    @JsonProperty("result_status")
    private String resultStatus;
    @JsonProperty("is_locked")
    private Boolean isLocked;

    @JsonProperty("birth_date")
    private String birthDate;
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String date) { this.birthDate = date; }

    public ExamRegistrationResponseDto() {}

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

    public UUID getExamRoomId() { return examRoomId; }
    public void setExamRoomId(UUID examRoomId) { this.examRoomId = examRoomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public Integer getRoomCapacity() { return roomCapacity; }
    public void setRoomCapacity(Integer roomCapacity) { this.roomCapacity = roomCapacity; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public UUID getResultId() { return resultId; }
    public void setResultId(UUID resultId) { this.resultId = resultId; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
}
