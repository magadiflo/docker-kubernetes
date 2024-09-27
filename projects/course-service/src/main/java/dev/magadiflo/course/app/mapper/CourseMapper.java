package dev.magadiflo.course.app.mapper;

import dev.magadiflo.course.app.model.dto.CourseRequest;
import dev.magadiflo.course.app.model.dto.CourseResponse;
import dev.magadiflo.course.app.model.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseMapper {
    Course toCourseEntity(CourseRequest courseRequest);

    CourseResponse toCourseResponse(Course course);

    @Mapping(target = "id", ignore = true)
    Course updateCourse(CourseRequest courseRequest, @MappingTarget Course course);
}
