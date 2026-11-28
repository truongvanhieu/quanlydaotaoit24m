package com.example.quanlydaotao.n7.dto.response;

import java.util.UUID;

public record ShiftOption(UUID id, String name, int startPeriod, int endPeriod) {
}
