package com.magadiflo.dk.business.domain.users.app.services;

import com.magadiflo.dk.business.domain.users.app.models.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<User> findAllUsers();

    Optional<User> findUserById(Long id);

    List<User> findAllById(Iterable<Long> ids);

    User saveUser(User user);

    Optional<User> updateUser(Long id, User userWithChangeData);

    Optional<Boolean> deleteUserById(Long id);
}
