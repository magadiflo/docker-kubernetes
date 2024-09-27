package dev.magadiflo.course.app.model.dto;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CourseResponse {
    private Long id;
    private String name;
}
