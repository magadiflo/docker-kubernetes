package dev.magadiflo.user.app.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", url = "http://course-service:8002", path = "/api/v1/courses")
public interface CourseFeignClient {
    @DeleteMapping(path = "/users/{userId}")
    void unassignUserFromAssociatedCourse(@PathVariable Long userId);
}
