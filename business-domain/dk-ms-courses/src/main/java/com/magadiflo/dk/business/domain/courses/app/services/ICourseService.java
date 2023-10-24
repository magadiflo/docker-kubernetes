package com.magadiflo.dk.business.domain.courses.app.services;

import com.magadiflo.dk.business.domain.courses.app.models.User;
import com.magadiflo.dk.business.domain.courses.app.models.entities.Course;

import java.util.List;
import java.util.Optional;

public interface ICourseService {
    List<Course> findAllCourses();

    Optional<Course> findCourseById(Long id);

    Optional<Course> findCourseByIdWithFullUsersDetails(Long id);

    Course saveCourse(Course course);

    Optional<Course> updateCourse(Long id, Course courseWithChangeData);

    Optional<Boolean> deleteCourseById(Long id);

    Optional<User> assignExistingUserToACourse(User user, Long courseId);

    Optional<User> createUserAndAssignToCourse(User user, Long courseId);

    Optional<User> unassigningAnExistingUserFromACourse(User user, Long courseId);

}
