package com.magadiflo.dk.business.domain.users.app.exceptions.domain;

public class EmailExistException extends RuntimeException {
    public EmailExistException(String message) {
        super(message);
    }
}
