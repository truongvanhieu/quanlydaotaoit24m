package com.example.quanlydaotao.n1.dto;

import java.util.List;
import java.util.UUID;

public record UserSearchResponse(
        UUID id,
        String fullName,
        String email,
        List<RoleSummaryResponse> roles) {
}
