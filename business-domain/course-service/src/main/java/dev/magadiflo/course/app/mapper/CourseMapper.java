package dev.magadiflo.course.app.mapper;

import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseMapper {
    CourseResponse toCourseResponse(Course course);

    Course toCourse(CourseRequest request);

    @Mapping(target = "id", ignore = true)
    Course toUpdateCourse(@MappingTarget Course course, CourseRequest request);
}
