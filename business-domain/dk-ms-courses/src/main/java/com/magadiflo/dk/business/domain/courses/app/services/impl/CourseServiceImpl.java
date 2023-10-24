package com.magadiflo.dk.business.domain.courses.app.services.impl;

import com.magadiflo.dk.business.domain.courses.app.clients.IUserFeignClient;
import com.magadiflo.dk.business.domain.courses.app.models.User;
import com.magadiflo.dk.business.domain.courses.app.models.entities.Course;
import com.magadiflo.dk.business.domain.courses.app.repositories.ICourseRepository;
import com.magadiflo.dk.business.domain.courses.app.services.ICourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements ICourseService {
    private final ICourseRepository courseRepository;
    private final IUserFeignClient userFeignClient;

    public CourseServiceImpl(ICourseRepository courseRepository, IUserFeignClient userFeignClient) {
        this.courseRepository = courseRepository;
        this.userFeignClient = userFeignClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAllCourses() {
        return (List<Course>) this.courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findCourseById(Long id) {
        return this.courseRepository.findById(id);
    }

    @Override
    @Transactional
    public Course saveCourse(Course course) {
        return this.courseRepository.save(course);
    }

    @Override
    @Transactional
    public Optional<Course> updateCourse(Long id, Course courseWithChangeData) {
        return this.courseRepository.findById(id)
                .map(courseDB -> {
                    courseDB.setName(courseWithChangeData.getName());
                    return courseDB;
                })
                .map(this.courseRepository::save);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteCourseById(Long id) {
        return this.courseRepository.findById(id)
                .map(courseDB -> {
                    this.courseRepository.deleteById(courseDB.getId());
                    return true;
                });
    }

    @Override
    public Optional<User> assignExistingUserToACourse(User user, Long courseId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> createUserAndAssignToCourse(User user, Long courseId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> unassigningAnExistingUserFromACourse(User user, Long courseId) {
        return Optional.empty();
    }
}
