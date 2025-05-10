package dev.magadiflo.course.app.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(@NotBlank
                            String name) {
}
