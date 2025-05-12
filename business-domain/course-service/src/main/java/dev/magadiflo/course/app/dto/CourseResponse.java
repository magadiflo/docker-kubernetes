package dev.magadiflo.course.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record CourseResponse(Long id,
                             String name,
                             @JsonInclude(JsonInclude.Include.NON_NULL)
                             List<UserResponse> users) {
}
