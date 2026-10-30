package com.example.quanlydaotao.n7.dto.request;

import java.util.UUID;

public record RoomRequest(
        UUID buildingId,
        String roomCode,
        String roomName,
        Integer floor,
        Integer capacity,
        String roomType,
        String status,
        Boolean hasProjector,
        Boolean hasAirConditioner,
        Boolean hasComputer,
        String description,
        Boolean active) {
}
