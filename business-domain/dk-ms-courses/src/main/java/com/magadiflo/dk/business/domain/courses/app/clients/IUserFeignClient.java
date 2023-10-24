package com.magadiflo.dk.business.domain.courses.app.clients;

import com.magadiflo.dk.business.domain.courses.app.models.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "dk-ms-users", url = "localhost:8001", path = "/api/v1/users")
public interface IUserFeignClient {
    @GetMapping(path = "/{id}")
    User getUser(@PathVariable Long id);

    @PostMapping
    User saveUser(@RequestBody User user);
}
