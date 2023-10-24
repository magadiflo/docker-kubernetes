package com.magadiflo.dk.business.domain.users.app.services.impl;

import com.magadiflo.dk.business.domain.users.app.clients.ICourseFeignClient;
import com.magadiflo.dk.business.domain.users.app.exceptions.domain.EmailExistException;
import com.magadiflo.dk.business.domain.users.app.models.entity.User;
import com.magadiflo.dk.business.domain.users.app.repositories.IUserRepository;
import com.magadiflo.dk.business.domain.users.app.services.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    private final ICourseFeignClient courseFeignClient;

    public UserServiceImpl(IUserRepository userRepository, ICourseFeignClient courseFeignClient) {
        this.userRepository = userRepository;
        this.courseFeignClient = courseFeignClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return (List<User>) this.userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllById(Iterable<Long> ids) {
        return (List<User>) this.userRepository.findAllById(ids);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new EmailExistException("Ya existe un usuario con ese email");
        }
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> updateUser(Long id, User userWithChangeData) {
        return this.userRepository.findById(id)
                .map(userDB -> {

                    if (!userWithChangeData.getEmail().equalsIgnoreCase(userDB.getEmail()) &&
                        this.userRepository.existsByEmail(userWithChangeData.getEmail())) {

                        throw new EmailExistException("Ya existe un usuario con ese email");
                    }

                    userDB.setName(userWithChangeData.getName());
                    userDB.setEmail(userWithChangeData.getEmail());
                    userDB.setPassword(userWithChangeData.getPassword());
                    return userDB;
                })
                .map(this.userRepository::save);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteUserById(Long id) {
        return this.userRepository.findById(id)
                .map(userDB -> {
                    this.userRepository.deleteById(userDB.getId());
                    this.courseFeignClient.unassigningUserByUserId(id);
                    return true;
                });
    }
}