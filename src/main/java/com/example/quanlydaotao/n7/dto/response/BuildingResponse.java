package com.example.quanlydaotao.n7.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record BuildingResponse(
        UUID id,
        String code,
        String name,
        String address,
        Integer totalFloors,
        String buildingType,
        String description,
        String note,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
