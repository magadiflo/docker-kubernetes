package dev.magadiflo.course.app.repository;

import dev.magadiflo.course.app.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
