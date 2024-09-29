package dev.magadiflo.course.app.service.impl;

import dev.magadiflo.course.app.repository.CourseUserRepository;
import dev.magadiflo.course.app.service.CourseUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseUserServiceImpl implements CourseUserService {

    private final CourseUserRepository courseUserRepository;

    @Override
    @Transactional
    public void deleteCourseUserByUserId(Long userId) {
        this.courseUserRepository.deleteByUserId(userId);
    }
}
