package dev.magadiflo.course.app.repository;

import dev.magadiflo.course.app.model.entity.CourseUser;
import org.springframework.data.repository.CrudRepository;

public interface CourseUserRepository extends CrudRepository<CourseUser, Long> {
    void deleteByUserId(Long userId);
}
