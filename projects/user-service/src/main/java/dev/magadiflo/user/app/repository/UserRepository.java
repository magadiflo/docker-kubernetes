package dev.magadiflo.user.app.repository;

import dev.magadiflo.user.app.model.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);
}
