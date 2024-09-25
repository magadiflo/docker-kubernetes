package dev.magadiflo.user.app.handler;

import dev.magadiflo.user.app.exception.EmailAlreadyExistsException;
import dev.magadiflo.user.app.exception.NotFoundException;
import dev.magadiflo.user.app.exception.UserNotFound;
import dev.magadiflo.user.app.util.HttpErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<HttpErrorResponse> handleNotFoundException(NotFoundException exception,
                                                                     HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(HttpErrorResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<HttpErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception,
                                                                               HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpErrorResponse.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception,
                                                                                   HttpServletRequest request) {
        Map<String, List<String>> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(defaultMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpErrorResponse.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .message("Falló la validación de los campos")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .errors(errors)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResponse> handleGenericException(Exception exception, HttpServletRequest request) {
        log.error("Ocurrió un error inesperado", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HttpErrorResponse.builder()
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build());
    }

}
