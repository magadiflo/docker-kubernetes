package dev.magadiflo.user.app.service;

import dev.magadiflo.user.app.dto.UserRequest;
import dev.magadiflo.user.app.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> findAllUsers();

    UserResponse findUser(Long userId);

    UserResponse saveUser(UserRequest userRequest);

    UserResponse updateUser(Long userId, UserRequest userRequest);

    void deleteUser(Long userId);
}
