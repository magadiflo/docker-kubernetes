package dev.magadiflo.course.app.service.impl;

import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.entity.Course;
import dev.magadiflo.course.app.exception.CourseNotFoundException;
import dev.magadiflo.course.app.mapper.CourseMapper;
import dev.magadiflo.course.app.repository.CourseRepository;
import dev.magadiflo.course.app.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    public List<CourseResponse> findAllCourses() {
        return this.courseRepository.findAll().stream()
                .map(this.courseMapper::toCourseResponse)
                .toList();
    }

    @Override
    public CourseResponse findCourse(Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(this.courseMapper::toCourseResponse)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    @Transactional
    public CourseResponse saveCourse(CourseRequest courseRequest) {
        Course courseDB = this.courseRepository.save(this.courseMapper.toCourse(courseRequest));
        return this.courseMapper.toCourseResponse(courseDB);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest courseRequest) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> this.courseMapper.toUpdateCourse(courseDB, courseRequest))
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
}
