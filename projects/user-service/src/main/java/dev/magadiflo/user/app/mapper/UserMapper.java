package dev.magadiflo.user.app.mapper;

import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import dev.magadiflo.user.app.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUserEntity(UserRequest userRequest);

    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    User updateUser(UserRequest userRequest, @MappingTarget User user);
}
