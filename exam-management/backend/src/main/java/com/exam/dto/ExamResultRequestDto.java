package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class ExamResultRequestDto {
    @JsonProperty("registration_id")
    private UUID registrationId;

    @JsonProperty("score")
    private BigDecimal score;

    @JsonProperty("status")
    private String status;

    @JsonProperty("appeal_status")
    private String appealStatus;

    @JsonProperty("is_locked")
    private Boolean isLocked;

    @JsonProperty("new_score")
    private BigDecimal newScore;

    public ExamResultRequestDto() {}

    public UUID getRegistrationId() { return registrationId; }
    public void setRegistrationId(UUID registrationId) { this.registrationId = registrationId; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAppealStatus() { return appealStatus; }
    public void setAppealStatus(String appealStatus) { this.appealStatus = appealStatus; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public BigDecimal getNewScore() { return newScore; }
    public void setNewScore(BigDecimal newScore) { this.newScore = newScore; }
}
