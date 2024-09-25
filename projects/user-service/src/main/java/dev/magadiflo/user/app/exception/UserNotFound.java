package dev.magadiflo.user.app.exception;

public class UserNotFound extends NotFoundException {
    public UserNotFound(Long userId) {
        super("No se encuentra el usuario con id [%d]".formatted(userId));
    }
}
