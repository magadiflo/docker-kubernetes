package dev.magadiflo.course.app.repository;

import dev.magadiflo.course.app.model.entity.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<Course, Long> {
}
