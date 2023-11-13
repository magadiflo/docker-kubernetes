package com.magadiflo.dk.business.domain.users.app.controllers;

import com.magadiflo.dk.business.domain.users.app.models.entity.User;
import com.magadiflo.dk.business.domain.users.app.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    private final IUserService userService;
    private final ApplicationContext context;

    public UserController(IUserService userService, ApplicationContext context) {
        this.userService = userService;
        this.context = context;
    }

    @GetMapping(path = "/cause-error")
    public void causeError() {
        ((ConfigurableApplicationContext) this.context).close();
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
