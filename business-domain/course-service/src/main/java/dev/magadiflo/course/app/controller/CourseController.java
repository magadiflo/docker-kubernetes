package dev.magadiflo.course.app.controller;

import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.service.CourseService;
import dev.magadiflo.course.app.service.CourseUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseUserService courseUserService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> findAllCourses() {
        return ResponseEntity.ok(this.courseService.findAllCourses());
    }

    @GetMapping(path = "/{courseId}")
    public ResponseEntity<CourseResponse> findCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(this.courseService.findCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> saveCourse(@Valid @RequestBody CourseRequest courseRequest) {
        CourseResponse courseResponse = this.courseService.saveCourse(courseRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{courseId}")
                .buildAndExpand(courseResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(courseResponse);
    }

    @PutMapping(path = "/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseRequest courseRequest) {
        return ResponseEntity.ok(this.courseService.updateCourse(courseId, courseRequest));
    }

    @DeleteMapping(path = "/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        this.courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{courseId}/users/{userId}")
    public ResponseEntity<UserResponse> assignExistingUserToCourse(@PathVariable Long courseId,
                                                                   @PathVariable Long userId) {
        return ResponseEntity.ok(this.courseService.assignExistingUserToCourse(userId, courseId));
    }

    @PostMapping(path = "/{courseId}/users")
    public ResponseEntity<UserResponse> createUserAndAssignItToCourse(@Valid @RequestBody UserRequest userRequest,
                                                                      @PathVariable Long courseId) {
        return new ResponseEntity<>(this.courseService.createUserAndAssignItToCourse(userRequest, courseId), HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{courseId}/users/{userId}")
    public ResponseEntity<UserResponse> unassignUserFromACourse(@PathVariable Long courseId, @PathVariable Long userId) {
        return ResponseEntity.ok(this.courseService.unassignUserFromACourse(userId, courseId));
    }

    @DeleteMapping(path = "/users/{userId}")
    public ResponseEntity<Void> unassignUserFromAssociatedCourse(@PathVariable Long userId) {
        this.courseUserService.deleteCourseUserByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
