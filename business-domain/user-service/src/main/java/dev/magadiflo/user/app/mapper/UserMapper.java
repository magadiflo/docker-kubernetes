package dev.magadiflo.user.app.mapper;

import dev.magadiflo.user.app.dto.UserRequest;
import dev.magadiflo.user.app.dto.UserResponse;
import dev.magadiflo.user.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserResponse toUserResponse(User user);

    User toUser(UserRequest request);

    @Mapping(target = "id", ignore = true)
    User toUpdateUser(@MappingTarget User user, UserRequest request);
}
