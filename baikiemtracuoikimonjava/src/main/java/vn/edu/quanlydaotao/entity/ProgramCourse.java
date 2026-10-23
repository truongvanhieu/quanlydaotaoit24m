package vn.edu.quanlydaotao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "program_courses", schema = "dbo")
public class ProgramCourse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "program_id", nullable = false)
	private TrainingProgram program;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(name = "semester_index", nullable = false)
	private int semesterIndex;

	@Column(name = "is_compulsory", nullable = false)
	private boolean compulsory;

	public ProgramCourse() {
	}

	public Long getId() {
		return id;
	}

	public TrainingProgram getProgram() {
		return program;
	}

	public void setProgram(TrainingProgram program) {
		this.program = program;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public int getSemesterIndex() {
		return semesterIndex;
	}

	public void setSemesterIndex(int semesterIndex) {
		this.semesterIndex = semesterIndex;
	}

	public boolean isCompulsory() {
		return compulsory;
	}

	public void setCompulsory(boolean compulsory) {
		this.compulsory = compulsory;
	}
}
