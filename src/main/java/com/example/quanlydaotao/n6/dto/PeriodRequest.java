package com.example.quanlydaotao.n6.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record PeriodRequest(String name, UUID semesterId, LocalDateTime startTime,
                            LocalDateTime endTime, String targetConfig, Short minCredits,
                            Short maxCredits, Boolean allowRetake, Boolean active) {}
