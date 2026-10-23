package vn.edu.quanlydaotao.exception;

import java.time.Instant;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(
			NotFoundException exception,
			HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(
			ConflictException exception,
			HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(
			BadRequestException exception,
			HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleInvalidBody(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return error(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolation(
			ConstraintViolationException exception,
			HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiError> handleUnreadableRequest(Exception exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Tham số hoặc JSON gửi lên không hợp lệ.", request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrity(
			DataIntegrityViolationException exception,
			HttpServletRequest request) {
		return error(
				HttpStatus.CONFLICT,
				"Dữ liệu bị trùng hoặc đang được dữ liệu khác tham chiếu.",
				request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(
			Exception exception,
			HttpServletRequest request) {
		logger.error("Lỗi không mong muốn khi xử lý {} {}", request.getMethod(), request.getRequestURI(), exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi máy chủ.", request);
	}

	private ResponseEntity<ApiError> error(
			HttpStatus status,
			String message,
			HttpServletRequest request) {
		ApiError response = new ApiError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI());
		return ResponseEntity.status(status).body(response);
	}
}
