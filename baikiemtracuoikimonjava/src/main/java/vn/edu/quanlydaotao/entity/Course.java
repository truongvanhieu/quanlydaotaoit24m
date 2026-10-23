package vn.edu.quanlydaotao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses", schema = "dbo")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "course_code", nullable = false, length = 20)
	private String courseCode;

	@Column(name = "course_name", nullable = false, length = 200)
	private String courseName;

	@Column(name = "credits", nullable = false)
	private int credits;

	@Column(name = "theory_credits", nullable = false)
	private int theoryCredits;

	@Column(name = "practice_credits", nullable = false)
	private int practiceCredits;

	@Column(name = "description", columnDefinition = "nvarchar(max)")
	private String description;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	public Course() {
	}

	public Long getId() {
		return id;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}

	public int getTheoryCredits() {
		return theoryCredits;
	}

	public void setTheoryCredits(int theoryCredits) {
		this.theoryCredits = theoryCredits;
	}

	public int getPracticeCredits() {
		return practiceCredits;
	}

	public void setPracticeCredits(int practiceCredits) {
		this.practiceCredits = practiceCredits;
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
}
