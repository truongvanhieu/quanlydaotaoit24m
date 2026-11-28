package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;

@Entity
@Table(name = "academic_years")
public class AcademicYear extends BaseEntity {
    @Column(name = "code", length = 100)
    private String code;

    @Nationalized
    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "`year`", length = 20)
    private String year;

    @Nationalized
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
