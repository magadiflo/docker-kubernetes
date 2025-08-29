package dev.magadiflo.course.app.mapper;

import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.entity.CourseUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseUserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "userId")
    CourseUser toCourseUser(UserResponse userResponse);
}
