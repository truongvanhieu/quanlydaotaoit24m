package vn.edu.quanlydaotao.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedResponse<T>(
		List<T> content,
		int page,
		int limit,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last) {

	public static <T> PagedResponse<T> from(Page<T> result) {
		return new PagedResponse<>(
				result.getContent(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages(),
				result.isFirst(),
				result.isLast());
	}
}
