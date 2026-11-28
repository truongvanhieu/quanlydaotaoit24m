package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ExamRoomResponseDto {
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
    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("room_id")
    private UUID roomId;
    @JsonProperty("room_number")
    private String roomNumber;
    @JsonProperty("building")
    private String building;
    @JsonProperty("room_max_capacity")
    private Integer roomMaxCapacity;
    @JsonProperty("capacity")
    private Integer capacity;
    @JsonProperty("current_registrations")
    private long currentRegistrations;

    public ExamRoomResponseDto() {}

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

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public Integer getRoomMaxCapacity() { return roomMaxCapacity; }
    public void setRoomMaxCapacity(Integer roomMaxCapacity) { this.roomMaxCapacity = roomMaxCapacity; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public long getCurrentRegistrations() { return currentRegistrations; }
    public void setCurrentRegistrations(long currentRegistrations) { this.currentRegistrations = currentRegistrations; }
}
