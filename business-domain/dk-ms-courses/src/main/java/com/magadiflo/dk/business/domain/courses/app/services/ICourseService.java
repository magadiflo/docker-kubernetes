package com.magadiflo.dk.business.domain.courses.app.services;

import com.magadiflo.dk.business.domain.courses.app.models.entities.Course;

import java.util.List;
import java.util.Optional;

public interface ICourseService {
    List<Course> findAllCourses();

    Optional<Course> findCourseById(Long id);

    Course saveCourse(Course course);

    Optional<Course> updateCourse(Long id, Course courseWithChangeData);

    Optional<Boolean> deleteCourseById(Long id);

}
