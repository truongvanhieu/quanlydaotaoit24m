package com.example.quanlydaotao.n7.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ApiExceptionHandlerTest {

    @Test
    void includesResponseStatusReasonInProblemDetail() {
        String reason = "Phòng B10101 bị trùng vào ngày 2027-02-08, tiết 1-3.";
        var response = new ApiExceptionHandler().handleResponseStatusException(
                new ResponseStatusException(HttpStatus.CONFLICT, reason));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(reason, response.getBody().getDetail());
    }
}
