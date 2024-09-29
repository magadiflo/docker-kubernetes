package dev.magadiflo.course.app.model.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CourseResponse {
    private Long id;
    private String name;
    @Builder.Default
    private List<UserResponse> users = new ArrayList<>();
}
