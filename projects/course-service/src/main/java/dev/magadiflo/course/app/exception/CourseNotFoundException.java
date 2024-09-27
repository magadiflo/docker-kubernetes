package dev.magadiflo.course.app.exception;

public class CourseNotFoundException extends NotFoundException {
    public CourseNotFoundException(Long courseId) {
        super("No se encuentra el curso con id [%d]".formatted(courseId));
    }
}
