package com.pf.attendance.api;

import com.pf.attendance.app.UnknownEmployeeException;
import com.pf.attendance.domain.PunchConflictException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(PunchConflictException.class)
  public ResponseEntity<Map<String, Object>> conflict(PunchConflictException ex) {
    return error(HttpStatus.CONFLICT, "conflict", ex.getMessage());
  }

  @ExceptionHandler(UnknownEmployeeException.class)
  public ResponseEntity<Map<String, Object>> unknown(UnknownEmployeeException ex) {
    return error(HttpStatus.UNAUTHORIZED, "unauthorized", ex.getMessage());
  }

  @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
  public ResponseEntity<Map<String, Object>> badRequest(Exception ex) {
    return error(HttpStatus.BAD_REQUEST, "validation_error", message(ex));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex) {
    return error(HttpStatus.BAD_REQUEST, "validation_error", "invalid request");
  }

  private static String message(Exception ex) {
    return ex.getMessage() == null || ex.getMessage().isBlank() ? "invalid request" : ex.getMessage();
  }

  private static ResponseEntity<Map<String, Object>> error(
      HttpStatus status, String code, String message) {
    return ResponseEntity.status(status)
        .body(Map.of("error", Map.of("code", code, "message", message)));
  }
}
