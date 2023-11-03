package com.magadiflo.dk.business.domain.users.app.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "dk-ms-courses", url = "${microservices.communication.dk-ms-courses.url}", path = "/api/v1/courses")
public interface ICourseFeignClient {
    @DeleteMapping(path = "/unassigning-user-by-userid/{userId}")
    void unassigningUserByUserId(@PathVariable Long userId);
}
