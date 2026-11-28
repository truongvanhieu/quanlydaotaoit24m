package com.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ExamPaperRequestDto {
    @JsonProperty("exam_id")
    private UUID examId;

    @JsonProperty("paper_code")
    private String paperCode;

    @JsonProperty("file_url")
    private String fileUrl;

    public ExamPaperRequestDto() {}

    public UUID getExamId() { return examId; }
    public void setExamId(UUID examId) { this.examId = examId; }

    public String getPaperCode() { return paperCode; }
    public void setPaperCode(String paperCode) { this.paperCode = paperCode; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
