package dev.magadiflo.user.app.service.impl;

import dev.magadiflo.user.app.client.CourseFeignClient;
import dev.magadiflo.user.app.exception.EmailAlreadyExistsException;
import dev.magadiflo.user.app.exception.UserNotFound;
import dev.magadiflo.user.app.mapper.UserMapper;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import dev.magadiflo.user.app.model.entity.User;
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
    private final CourseFeignClient courseFeignClient;

    @Override
    public List<UserResponse> findAllUsers() {
        return ((List<User>) this.userRepository.findAll()).stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse findUser(Long userId) {
        return this.userRepository.findById(userId)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFound(userId));
    }

    @Override
    @Transactional
    public UserResponse saveUser(UserRequest userRequest) {
        if (this.userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }
        User userDB = this.userRepository.save(this.userMapper.toUserEntity(userRequest));
        return this.userMapper.toUserResponse(userDB);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        return this.userRepository.findById(userId)
                .map(userDB -> {
                    if (!userRequest.getEmail().equalsIgnoreCase(userDB.getEmail()) &&
                        this.userRepository.existsByEmail(userRequest.getEmail())) {
                        throw new EmailAlreadyExistsException(userRequest.getEmail());
                    }
                    return this.userMapper.updateUser(userRequest, userDB);
                })
                .map(this.userRepository::save)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFound(userId));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User userDB = this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound(userId));
        this.userRepository.delete(userDB);
        this.courseFeignClient.unassignUserFromAssociatedCourse(userId);
    }

    @Override
    public List<UserResponse> findUsersByIds(List<Long> userIds) {
        return ((List<User>) this.userRepository.findAllById(userIds)).stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }
}
