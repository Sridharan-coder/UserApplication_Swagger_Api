package com.example.forgetPassword.Exception;

import java.io.IOException;
import java.time.DateTimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.forgetPassword.Exception.CustomException.InvalidDataException;
import com.example.forgetPassword.Exception.CustomException.SignInFailedException;
import com.example.forgetPassword.Exception.CustomException.UserException;
import com.example.forgetPassword.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(exception = InvalidDataException.class)
	public ResponseEntity<ErrorResponse> handleInvalidEmailAddress(InvalidDataException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(),
				HttpStatus.UNPROCESSABLE_ENTITY.value());
		logger.error(String.format("InvalidDataException for %s", exception.getMessage()));
		return ResponseEntity.unprocessableEntity().body(errorResponse);
	}

	@ExceptionHandler(exception = UserException.class)
	public ResponseEntity<ErrorResponse> handleUserNameNotFound(UserException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND.value());
		logger.error(String.format("UserException for %s", exception.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(exception = SignInFailedException.class)
	public ResponseEntity<ErrorResponse> handleSigninFailed(SignInFailedException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND.value());
		logger.error(String.format("SignInFailedException for %s", exception.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(exception = MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleArgunmentMisMatch(MethodArgumentNotValidException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getFieldError().getDefaultMessage(),
				exception.getStatusCode().value());
		logger.error(String.format("MethodArgumentNotValidException for %s", exception.getMessage()));
		return ResponseEntity.status(exception.getStatusCode()).body(errorResponse);
	}

	@ExceptionHandler(exception = RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND.value());
		logger.error(String.format("RuntimeException for %s", exception.getMessage()));
		return ResponseEntity.internalServerError().body(errorResponse);
	}

	@ExceptionHandler(exception = IOException.class)
	public ResponseEntity<ErrorResponse> handleArgunmentMisMatch(IOException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(), 500);
		logger.error(String.format("IOException for %s", exception.getMessage()));
		return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
	}

	@ExceptionHandler(exception = Exception.class)
	public ResponseEntity<ErrorResponse> handleArgunmentMisMatch(Exception exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(), 500);
		logger.error(String.format("MethodArgumentNotValidException for %s", exception.getMessage()));
		return ResponseEntity.status(500).body(errorResponse);
	}

	@ExceptionHandler(exception = DateTimeException.class)
	public ResponseEntity<ErrorResponse> handleDateTimeException(DateTimeException exception) {
		ErrorResponse errorResponse = new ErrorResponse(false, exception.getMessage(), 410);
		logger.error(String.format("IOException for %s", exception.getMessage()));
		return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
	}

}
