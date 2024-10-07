package dev.magadiflo.course.app.client;

import dev.magadiflo.course.app.model.dto.UserRequest;
import dev.magadiflo.course.app.model.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "user-service", url = "http://user-service:8001", path = "/api/v1/users")
public interface UserFeignClient {
    @GetMapping(path = "/{userId}")
    UserResponse findUser(@PathVariable Long userId);

    @PostMapping
    UserResponse saveUser(@RequestBody UserRequest userRequest);

    @GetMapping(path = "/by-ids")
    List<UserResponse> findUsersByIds(@RequestParam List<Long> userIds);
}
