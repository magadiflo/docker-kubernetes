package dev.magadiflo.user.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(@NotBlank
                          String name,
                          @NotBlank
                          @Email
                          String email,
                          @NotBlank
                          String password) {
}
