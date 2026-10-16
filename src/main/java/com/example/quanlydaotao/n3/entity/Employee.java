
package com.example.quanlydaotao.n3.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "N3Employee")
@Table(name = "employees", schema = "dbo")
public class Employee {

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

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "department_id")
    private UUID departmentId;

    // SQL thuc te: NVARCHAR(100), khong phai UUID
    @Column(name = "position_id", length = 100)
    private String positionId;

    // SQL thuc te: DATETIME2
    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "contract_type", length = 255)
    private String contractType;

    @Column(name = "salary_coefficient", precision = 4, scale = 2)
    private BigDecimal salaryCoefficient;

    @Column(name = "academic_degree", length = 50)
    private String academicDegree;

    @Column(name = "academic_title", length = 50)
    private String academicTitle;

    @Column(name = "specialization", length = 255)
    private String specialization;

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
    private Boolean isActive;

    public Employee() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Chi sao chep du lieu nghiep vu
    // Khong cho request ghi de ID hoac thong tin audit
    public void copyBusinessFields(Employee source) {
        this.userId = source.userId;
        this.code = source.code;
        this.fullName = source.fullName;
        this.dateOfBirth = source.dateOfBirth;
        this.gender = source.gender;
        this.email = source.email;
        this.phone = source.phone;
        this.address = source.address;
        this.departmentId = source.departmentId;
        this.positionId = source.positionId;
        this.hireDate = source.hireDate;
        this.contractType = source.contractType;
        this.salaryCoefficient = source.salaryCoefficient;
        this.academicDegree = source.academicDegree;
        this.academicTitle = source.academicTitle;
        this.specialization = source.specialization;
        this.isActive = source.isActive;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public LocalDateTime getHireDate() { return hireDate; }
    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public String getContractType() { return contractType; }
    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public BigDecimal getSalaryCoefficient() {
        return salaryCoefficient;
    }
    public void setSalaryCoefficient(BigDecimal value) {
        this.salaryCoefficient = value;
    }

    public String getAcademicDegree() { return academicDegree; }
    public void setAcademicDegree(String value) {
        this.academicDegree = value;
    }

    public String getAcademicTitle() { return academicTitle; }
    public void setAcademicTitle(String value) {
        this.academicTitle = value;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String value) {
        this.specialization = value;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) {
        this.updatedAt = value;
    }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID value) {
        this.createdBy = value;
    }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID value) {
        this.updatedBy = value;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime value) {
        this.deletedAt = value;
    }

    public UUID getDeletedBy() { return deletedBy; }
    public void setDeletedBy(UUID value) {
        this.deletedBy = value;
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
}
