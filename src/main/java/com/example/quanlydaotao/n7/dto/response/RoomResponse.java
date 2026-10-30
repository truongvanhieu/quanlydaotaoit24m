package com.example.quanlydaotao.n7.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        UUID buildingId,
        String buildingCode,
        String buildingName,
        String roomCode,
        String roomName,
        Integer floor,
        Integer capacity,
        String roomType,
        String status,
        boolean hasProjector,
        boolean hasAirConditioner,
        boolean hasComputer,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
