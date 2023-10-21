package com.magadiflo.dk.business.domain.users.app.repositories;

import com.magadiflo.dk.business.domain.users.app.models.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface IUserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);
}
