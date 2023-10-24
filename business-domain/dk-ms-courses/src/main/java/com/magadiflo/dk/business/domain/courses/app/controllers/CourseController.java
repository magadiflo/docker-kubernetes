package com.magadiflo.dk.business.domain.courses.app.controllers;

import com.magadiflo.dk.business.domain.courses.app.models.User;
import com.magadiflo.dk.business.domain.courses.app.models.entities.Course;
import com.magadiflo.dk.business.domain.courses.app.services.ICourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {
    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(this.courseService.findAllCourses());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return this.courseService.findCourseByIdWithFullUsersDetails(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> saveCourse(@Valid @RequestBody Course course) {
        Course courseDB = this.courseService.saveCourse(course);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(courseDB.getId())
                .toUri();
        return ResponseEntity.created(location).body(courseDB);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody Course course) {
        return this.courseService.updateCourse(id, course)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        return this.courseService.deleteCourseById(id)
                .map(wasDeleted -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/assign-user-to-course/{courseId}")
    public ResponseEntity<User> assignExistingUserToACourse(@RequestBody User user, @PathVariable Long courseId) {
        return this.courseService.assignExistingUserToACourse(user, courseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/create-user-and-assign-to-course/{courseId}")
    public ResponseEntity<User> createUserAndAssignToCourse(@RequestBody User user, @PathVariable Long courseId) {
        return this.courseService.createUserAndAssignToCourse(user, courseId)
                .map(userDB -> {
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(userDB.id())
                            .toUri();
                    return ResponseEntity.created(location).body(userDB);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/unassigning-user-from-a-course/{courseId}")
    public ResponseEntity<User> unassigningAnExistingUserFromACourse(@RequestBody User user, @PathVariable Long courseId) {
        return this.courseService.unassigningAnExistingUserFromACourse(user, courseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
