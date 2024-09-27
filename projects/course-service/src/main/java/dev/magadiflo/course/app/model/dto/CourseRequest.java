package dev.magadiflo.course.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CourseRequest {
    @NotBlank
    private String name;
}
