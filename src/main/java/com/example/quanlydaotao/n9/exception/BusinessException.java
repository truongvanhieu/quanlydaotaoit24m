package com.example.quanlydaotao.n9.exception;

/**
 * Lỗi nghiệp vụ của nhóm Học phí - Tài chính (N9).
 * Message sẽ được trả về cho client để hiển thị.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
