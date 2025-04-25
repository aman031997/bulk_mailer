package in.syncboard.bulkmail.exception;

import in.syncboard.bulkmail.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileException.class)
    public Mono<ResponseEntity<APIResponse<Object>>> handleProfileException(ProfileException ex) {
        APIResponse<Object> response = APIResponse.builder()
                .success(false)
                .statusCode(ex.getStatus().value())
                .message(ex.getMessage())
                .errors(Collections.singletonList(ex.getMessage()))
                .build();

        return Mono.just(ResponseEntity.status(ex.getStatus()).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<APIResponse<Object>>> handleValidationErrors(WebExchangeBindException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        APIResponse<Object> response = APIResponse.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();

        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<APIResponse<Object>>> handleGenericException(Exception ex) {
        APIResponse<Object> response = APIResponse.builder()
                .success(false)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .errors(Collections.singletonList(ex.getMessage()))
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}
