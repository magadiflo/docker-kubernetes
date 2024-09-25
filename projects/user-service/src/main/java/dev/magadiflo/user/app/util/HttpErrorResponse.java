package dev.magadiflo.user.app.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class HttpErrorResponse {
    private HttpStatus httpStatus;

    private String message;

    private LocalDateTime timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;
}
