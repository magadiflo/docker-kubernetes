package dev.magadiflo.course.app.exception;

public class RemoteUserNotFoundException extends RuntimeException {
    public RemoteUserNotFoundException(Long userId) {
        super("El usuario con id %d no fue encontrado en el user-service.".formatted(userId));
    }
}
