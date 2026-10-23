package vn.edu.quanlydaotao.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_programs", schema = "dbo")
public class TrainingProgram {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "program_code", nullable = false, length = 30)
	private String programCode;

	@Column(name = "program_name", nullable = false, length = 200)
	private String programName;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "major_id", nullable = false)
	private Major major;

	@Column(name = "cohort", length = 20)
	private String cohort;

	@Column(name = "academic_year", length = 20)
	private String academicYear;

	@Column(name = "total_credits", nullable = false)
	private int totalCredits;

	@Column(name = "description", columnDefinition = "nvarchar(max)")
	private String description;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
	private List<ProgramCourse> programCourses = new ArrayList<>();

	public TrainingProgram() {
	}

	public Long getId() {
		return id;
	}

	public String getProgramCode() {
		return programCode;
	}

	public void setProgramCode(String programCode) {
		this.programCode = programCode;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public Major getMajor() {
		return major;
	}

	public void setMajor(Major major) {
		this.major = major;
	}

	public String getCohort() {
		return cohort;
	}

	public void setCohort(String cohort) {
		this.cohort = cohort;
	}

	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}

	public int getTotalCredits() {
		return totalCredits;
	}

	public void setTotalCredits(int totalCredits) {
		this.totalCredits = totalCredits;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<ProgramCourse> getProgramCourses() {
		return programCourses;
	}
}
