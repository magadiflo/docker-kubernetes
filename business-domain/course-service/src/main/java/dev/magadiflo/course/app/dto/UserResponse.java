package dev.magadiflo.course.app.dto;

public record UserResponse(Long id,
                           String name,
                           String email,
                           String password) {
}
