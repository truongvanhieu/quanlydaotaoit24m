package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "semesters")
public class Semester extends BaseEntity {
    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "school_year_name", length = 100)
    private String schoolYearName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "school_year_id")
    private SchoolYear schoolYear;

    // Getters & Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchoolYearName() { return schoolYearName; }
    public void setSchoolYearName(String schoolYearName) { this.schoolYearName = schoolYearName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public SchoolYear getSchoolYear() { return schoolYear; }
    public void setSchoolYear(SchoolYear schoolYear) {
        this.schoolYear = schoolYear;
        if (schoolYear != null && (this.schoolYearName == null || this.schoolYearName.isBlank())) {
            this.schoolYearName = schoolYear.getName();
        }
    }
}
