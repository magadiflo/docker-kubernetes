package com.magadiflo.dk.business.domain.users.app.controllers;

import com.magadiflo.dk.business.domain.users.app.models.entity.User;
import com.magadiflo.dk.business.domain.users.app.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    private final IUserService userService;
    private final ApplicationContext context;
    private final Environment environment;

    public UserController(IUserService userService, ApplicationContext context, Environment environment) {
        this.userService = userService;
        this.context = context;
        this.environment = environment;
    }

    @GetMapping(path = "/cause-error")
    public void causeError() {
        ((ConfigurableApplicationContext) this.context).close();
    }

    @GetMapping(path = "/info")
    public ResponseEntity<Map<String, Object>> get() {
        Map<String, Object> body = new HashMap<>();
        body.put("users", this.userService.findAllUsers());
        body.put("podName", this.environment.getProperty("POD_NAME"));
        body.put("podIP", this.environment.getProperty("POD_IP"));
        body.put("text", this.environment.getProperty("config.text"));
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(this.userService.findAllUsers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return this.userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/group")
    public ResponseEntity<List<User>> findAllById(@RequestParam List<Long> userIds) {
        return ResponseEntity.ok(this.userService.findAllById(userIds));
    }

    @PostMapping
    public ResponseEntity<User> saveUser(@Valid @RequestBody User user) {
        User userDB = this.userService.saveUser(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userDB.getId())
                .toUri();
        return ResponseEntity.created(location).body(userDB);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return this.userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return this.userService.deleteUserById(id)
                .map(wasDeleted -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
