package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ExamRegistrationRequestDto {
    @JsonProperty("registration_ids")
    private java.util.List<UUID> registrationIds;
    public java.util.List<UUID> getRegistrationIds() { return registrationIds; }
    public void setRegistrationIds(java.util.List<UUID> ids) { this.registrationIds = ids; }

    @JsonProperty("exam_id")
    private UUID examId;

    @JsonProperty("exam_room_id")
    private UUID examRoomId;

    @JsonProperty("student_id")
    private UUID studentId;

    @JsonProperty("roll_number")
    private String rollNumber;

    @JsonProperty("physical_room_id")
    private UUID physicalRoomId;
    @JsonProperty("student_ids")
    private java.util.List<UUID> studentIds;
    @JsonProperty("birth_date")
    private String birthDate;
    public UUID getPhysicalRoomId() { return physicalRoomId; }
    public void setPhysicalRoomId(UUID id) { this.physicalRoomId = id; }
    public java.util.List<UUID> getStudentIds() { return studentIds; }
    public void setStudentIds(java.util.List<UUID> ids) { this.studentIds = ids; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String date) { this.birthDate = date; }

    public ExamRegistrationRequestDto() {}

    public UUID getExamId() { return examId; }
    public void setExamId(UUID examId) { this.examId = examId; }

    public UUID getExamRoomId() { return examRoomId; }
    public void setExamRoomId(UUID examRoomId) { this.examRoomId = examRoomId; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
}
