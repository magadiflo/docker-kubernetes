package dev.magadiflo.user.app.model.dto;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String password;
}
