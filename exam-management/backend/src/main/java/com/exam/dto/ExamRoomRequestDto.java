package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ExamRoomRequestDto {
    @JsonProperty("exam_id")
    private UUID examId;

    @JsonProperty("room_id")
    private UUID roomId;

    @JsonProperty("capacity")
    private Integer capacity;

    // Thông tin phòng vật lý (chỉ sử dụng khi cập nhật)
    @JsonProperty("room_number")
    private String roomNumber;

    @JsonProperty("building")
    private String building;

    @JsonProperty("room_max_capacity")
    private Integer roomMaxCapacity;

    public ExamRoomRequestDto() {}

    public UUID getExamId() { return examId; }
    public void setExamId(UUID examId) { this.examId = examId; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public Integer getRoomMaxCapacity() { return roomMaxCapacity; }
    public void setRoomMaxCapacity(Integer roomMaxCapacity) { this.roomMaxCapacity = roomMaxCapacity; }
}
