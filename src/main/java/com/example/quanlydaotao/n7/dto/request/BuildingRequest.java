package com.example.quanlydaotao.n7.dto.request;

public record BuildingRequest(
        String code,
        String name,
        String address,
        Integer totalFloors,
        String buildingType,
        String description,
        String note,
        Boolean active) {
}
