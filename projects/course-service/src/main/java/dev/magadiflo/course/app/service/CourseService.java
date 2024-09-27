package dev.magadiflo.course.app.service;

import dev.magadiflo.course.app.model.dto.CourseRequest;
import dev.magadiflo.course.app.model.dto.CourseResponse;

import java.util.List;

public interface CourseService {
    List<CourseResponse> findAllCourses();

    CourseResponse findCourse(Long courseId);

    CourseResponse saveCourse(CourseRequest courseRequest);

    CourseResponse updateCourse(Long courseId, CourseRequest courseRequest);

    void deleteCourse(Long courseId);
}
