package dev.magadiflo.course.app.service.impl;

import dev.magadiflo.course.app.client.UserFeignClient;
import dev.magadiflo.course.app.exception.CourseNotFoundException;
import dev.magadiflo.course.app.mapper.CourseMapper;
import dev.magadiflo.course.app.mapper.CourseUserMapper;
import dev.magadiflo.course.app.model.dto.*;
import dev.magadiflo.course.app.model.entity.Course;
import dev.magadiflo.course.app.model.entity.CourseUser;
import dev.magadiflo.course.app.repository.CourseRepository;
import dev.magadiflo.course.app.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseUserMapper courseUserMapper;
    private final UserFeignClient userFeignClient;

    @Override
    public List<CourseResponse> findAllCourses(boolean loadRelations) {
        List<Course> courses = (List<Course>) this.courseRepository.findAll();
        return loadRelations ?
                courses.stream().map(this::loadRelations).toList() :
                courses.stream().map(this.courseMapper::toCourseResponse).toList();
    }

    @Override
    public CourseResponse findCourse(Long courseId, boolean loadRelations) {
        Course courseDB = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return loadRelations ?
                this.loadRelations(courseDB) :
                this.courseMapper.toCourseResponse(courseDB);
    }

    @Override
    @Transactional
    public CourseResponse saveCourse(CourseRequest courseRequest) {
        Course courseDB = this.courseRepository.save(this.courseMapper.toCourseEntity(courseRequest));
        return this.courseMapper.toCourseResponse(courseDB);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest courseRequest) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> this.courseMapper.updateCourse(courseRequest, courseDB))
                .map(this.courseRepository::save)
                .map(this.courseMapper::toCourseResponse)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course courseDB = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        this.courseRepository.delete(courseDB);
    }

    @Override
    @Transactional
    public UserResponse assignExistingUserToCourse(Long userId, Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> {
                    UserResponse userResponse = this.userFeignClient.findUser(userId);
                    CourseUser courseUser = this.courseUserMapper.toCourseUser(userResponse);
                    this.courseMapper.addCourseUserToCourse(courseUser, courseDB);
                    this.courseRepository.save(courseDB);
                    return userResponse;
                })
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    @Transactional
    public UserResponse createUserAndAssignItToCourse(UserRequest userRequest, Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> {
                    UserResponse userResponse = this.userFeignClient.saveUser(userRequest);
                    CourseUser courseUser = this.courseUserMapper.toCourseUser(userResponse);
                    this.courseMapper.addCourseUserToCourse(courseUser, courseDB);
                    this.courseRepository.save(courseDB);
                    return userResponse;
                })
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    @Transactional
    public UserResponse unassignUserFromACourse(Long userId, Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> {
                    UserResponse userResponse = this.userFeignClient.findUser(userId);
                    CourseUser courseUser = this.courseUserMapper.toCourseUser(userResponse);
                    this.courseMapper.deleteCourseUserFromCourse(courseUser, courseDB);
                    this.courseRepository.save(courseDB);
                    return userResponse;
                })
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private CourseResponse loadRelations(Course course) {
        Collection<Long> userIds = this.courseMapper.extractUserIdsFromCourse(course);
        CourseResponse courseResponse = this.courseMapper.toCourseResponse(course);
        List<UserResponse> usersResponseByIds = new ArrayList<>();

        if (!userIds.isEmpty()) {
            usersResponseByIds = this.userFeignClient.findUsersByIds((List<Long>) userIds);
        }
        courseResponse.setUsers(usersResponseByIds);

        return courseResponse;
    }
}
