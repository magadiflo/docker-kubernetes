package dev.magadiflo.course.app.service;

import dev.magadiflo.course.app.model.dto.CourseRequest;
import dev.magadiflo.course.app.model.dto.CourseResponse;
import dev.magadiflo.course.app.model.dto.UserRequest;
import dev.magadiflo.course.app.model.dto.UserResponse;

import java.util.List;

public interface CourseService {
    List<CourseResponse> findAllCourses();

    CourseResponse findCourse(Long courseId);

    CourseResponse saveCourse(CourseRequest courseRequest);

    CourseResponse updateCourse(Long courseId, CourseRequest courseRequest);

    void deleteCourse(Long courseId);

    UserResponse assignExistingUserToCourse(Long userId, Long courseId);

    UserResponse createUserAndAssignItToCourse(UserRequest userRequest, Long courseId);

    UserResponse unassignUserFromACourse(Long userId, Long courseId);
}
