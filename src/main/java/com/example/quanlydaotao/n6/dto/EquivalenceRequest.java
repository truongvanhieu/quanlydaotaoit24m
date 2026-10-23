package com.example.quanlydaotao.n6.dto;
import java.time.LocalDate;
import java.util.UUID;
public record EquivalenceRequest(UUID originalCourseId, UUID equivalentCourseId,
                                 Short equivalenceType, LocalDate effectDate,
                                 Boolean active, String note) {}
