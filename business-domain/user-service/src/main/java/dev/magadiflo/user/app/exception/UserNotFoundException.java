package dev.magadiflo.user.app.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super("No se encuentra el usuario con id [%d]".formatted(userId));
    }
}
