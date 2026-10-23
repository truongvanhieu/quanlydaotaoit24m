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
@Table(name = "course_prerequisites", schema = "dbo")
public class CoursePrerequisite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "prerequisite_course_id", nullable = false)
	private Course prerequisiteCourse;

	@Column(name = "type", nullable = false, length = 30)
	private String type;

	public CoursePrerequisite() {
	}

	public Long getId() {
		return id;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Course getPrerequisiteCourse() {
		return prerequisiteCourse;
	}

	public void setPrerequisiteCourse(Course prerequisiteCourse) {
		this.prerequisiteCourse = prerequisiteCourse;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
