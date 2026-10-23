package vn.edu.quanlydaotao.dto;

import vn.edu.quanlydaotao.entity.Major;

public record MajorResponse(
		Long id,
		String majorCode,
		String majorName,
		String description,
		boolean isActive) {

	public static MajorResponse from(Major major) {
		return new MajorResponse(
				major.getId(),
				major.getMajorCode(),
				major.getMajorName(),
				major.getDescription(),
				major.isActive());
	}
}
