package dev.magadiflo.user.app.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("El correo [%s] ya está asociado a otro usuario".formatted(email));
    }
}
