package com.magadiflo.dk.business.domain.courses.app.repositories;

import com.magadiflo.dk.business.domain.courses.app.models.entities.Course;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ICourseRepository extends CrudRepository<Course, Long> {

    @Modifying
    @Query("""
            DELETE FROM CourseUser AS cu
            WHERE cu.userId = :userId
            """)
    void deleteCurseUserById(@Param("userId") Long userId);
}
