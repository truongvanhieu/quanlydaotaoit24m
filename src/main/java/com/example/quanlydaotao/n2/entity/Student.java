package com.example.quanlydaotao.n2.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender; // 1: Nam, 2: Nữ, 0: Khác

    @Column(name = "personal_identification_number", length = 20)
    private String personalIdentificationNumber; // CMND/CCCD

    @Column(name = "date_of_issue")
    private LocalDate dateOfIssue;

    @Column(name = "card_place", length = 100)
    private String cardPlace;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "current_address", length = 300)
    private String currentAddress;

    @Column(name = "academic_year_year")
    private UUID academicYearYear;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "major_id")
    private UUID majorId;

    @Column(name = "training_program_id")
    private UUID trainingProgramId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "student_classess_id")
    private UUID studentClassessId;

    @Column(name = "admission_year")
    private LocalDateTime admissionYear;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Student() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStudentCode() {
        return code;
    }

    public void setStudentCode(String studentCode) {
        if (this.code == null || this.code.isEmpty()) {
            this.code = studentCode;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPersonalIdentificationNumber() {
        return personalIdentificationNumber;
    }

    public void setPersonalIdentificationNumber(String personalIdentificationNumber) {
        this.personalIdentificationNumber = personalIdentificationNumber;
    }

    public LocalDate getDateOfIssue() {
        return dateOfIssue;
    }

    public void setDateOfIssue(LocalDate dateOfIssue) {
        this.dateOfIssue = dateOfIssue;
    }

    public String getCardPlace() {
        return cardPlace;
    }

    public void setCardPlace(String cardPlace) {
        this.cardPlace = cardPlace;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public UUID getAcademicYearYear() {
        return academicYearYear;
    }

    public void setAcademicYearYear(UUID academicYearYear) {
        this.academicYearYear = academicYearYear;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getMajorId() {
        return majorId;
    }

    public void setMajorId(UUID majorId) {
        this.majorId = majorId;
    }

    public UUID getTrainingProgramId() {
        return trainingProgramId;
    }

    public void setTrainingProgramId(UUID trainingProgramId) {
        this.trainingProgramId = trainingProgramId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getStudentClassessId() {
        return studentClassessId;
    }

    public void setStudentClassessId(UUID studentClassessId) {
        this.studentClassessId = studentClassessId;
    }

    public LocalDateTime getAdmissionYear() {
        return admissionYear;
    }

    public void setAdmissionYear(LocalDateTime admissionYear) {
        this.admissionYear = admissionYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public UUID getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(UUID deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}