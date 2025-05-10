package dev.magadiflo.course.app;

import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

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
}
