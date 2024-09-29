package dev.magadiflo.course.app.client;

import dev.magadiflo.course.app.model.dto.UserRequest;
import dev.magadiflo.course.app.model.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "user-service", url = "http://localhost:8001", path = "/api/v1/users")
public interface UserFeignClient {
    @GetMapping(path = "/{userId}")
    UserResponse findUser(@PathVariable Long userId);

    @PostMapping
    UserResponse saveUser(@RequestBody UserRequest userRequest);
}
