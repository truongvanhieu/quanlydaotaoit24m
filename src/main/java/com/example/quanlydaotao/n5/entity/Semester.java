package com.example.quanlydaotao.n5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "semesters")
public class Semester extends BaseEntity {
    @Column(name = "code", length = 100)
    private String code;

    @Nationalized
    @Column(name = "name", length = 255)
    private String name;

    @Nationalized
    @Column(name = "school_year_name", length = 100)
    private String schoolYearName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "school_year_id")
    private SchoolYear schoolYear;

    @Transient
    private UUID transientSchoolYearId;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchoolYearName() {
        if (schoolYearName != null && !schoolYearName.isBlank()) {
            return schoolYearName;
        }
        return schoolYear != null ? schoolYear.getName() : null;
    }
    public void setSchoolYearName(String schoolYearName) { this.schoolYearName = schoolYearName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public SchoolYear getSchoolYear() { return schoolYear; }
    public void setSchoolYear(SchoolYear schoolYear) {
        this.schoolYear = schoolYear;
        if (schoolYear != null) {
            this.transientSchoolYearId = schoolYear.getId();
            if (this.schoolYearName == null || this.schoolYearName.isBlank()) {
                this.schoolYearName = schoolYear.getName();
            }
        }
    }

    public UUID getSchoolYearId() {
        if (schoolYear != null && schoolYear.getId() != null) {
            return schoolYear.getId();
        }
        return transientSchoolYearId;
    }

    public void setSchoolYearId(UUID schoolYearId) {
        this.transientSchoolYearId = schoolYearId;
    }
}
