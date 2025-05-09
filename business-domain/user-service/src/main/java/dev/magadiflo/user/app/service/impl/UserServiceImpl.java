package dev.magadiflo.user.app.service.impl;

import dev.magadiflo.user.app.dto.UserRequest;
import dev.magadiflo.user.app.dto.UserResponse;
import dev.magadiflo.user.app.entity.User;
import dev.magadiflo.user.app.exception.EmailAlreadyExistsException;
import dev.magadiflo.user.app.exception.UserNotFoundException;
import dev.magadiflo.user.app.mapper.UserMapper;
import dev.magadiflo.user.app.repository.UserRepository;
import dev.magadiflo.user.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> findAllUsers() {
        return this.userRepository.findAll().stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse findUser(Long userId) {
        return this.userRepository.findById(userId)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public UserResponse saveUser(UserRequest userRequest) {
        if (this.userRepository.existsByEmail(userRequest.email())) {
            throw new EmailAlreadyExistsException(userRequest.email());
        }
        User userDB = this.userRepository.save(this.userMapper.toUser(userRequest));
        return this.userMapper.toUserResponse(userDB);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        return this.userRepository.findById(userId)
                .map(userDB -> {
                    if (!userRequest.email().equalsIgnoreCase(userDB.getEmail()) &&
                            this.userRepository.existsByEmail(userRequest.email())) {
                        throw new EmailAlreadyExistsException(userRequest.email());
                    }
                    return this.userMapper.toUpdateUser(userDB, userRequest);
                })
                .map(this.userRepository::save)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User userDB = this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        this.userRepository.delete(userDB);
    }
}
