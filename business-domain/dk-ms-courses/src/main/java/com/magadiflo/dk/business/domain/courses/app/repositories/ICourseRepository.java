package com.magadiflo.dk.business.domain.courses.app.repositories;

import com.magadiflo.dk.business.domain.courses.app.models.entities.Course;
import org.springframework.data.repository.CrudRepository;

public interface ICourseRepository extends CrudRepository<Course, Long> {
}
