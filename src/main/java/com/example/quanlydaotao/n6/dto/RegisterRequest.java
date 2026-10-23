package com.example.quanlydaotao.n6.dto;
import java.util.UUID;
public record RegisterRequest(UUID studentId, UUID courseClassId,
                              UUID registrationPeriodId, Short registrationType,
                              UUID replacedGradeId) {}
