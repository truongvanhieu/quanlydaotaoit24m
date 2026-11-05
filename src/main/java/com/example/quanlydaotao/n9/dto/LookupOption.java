package com.example.quanlydaotao.n9.dto;

import java.util.UUID;

/**
 * Một lựa chọn cho hộp chọn (dropdown): id dùng để gửi lên server,
 * code và name dùng để hiển thị cho người dùng.
 */
public record LookupOption(UUID id, String code, String name) {
}
