package dev.magadiflo.course.app.exception;

public class CommunicationException extends RuntimeException {
    public CommunicationException(String message) {
        super("Se produjo un error en el user-service: %s".formatted(message));
    }
}
