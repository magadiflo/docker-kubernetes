package dev.magadiflo.course.app.repository;

import dev.magadiflo.course.app.entity.CourseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseUserRepository extends JpaRepository<CourseUser, Long> {
    void deleteByUserId(Long userId);
}
