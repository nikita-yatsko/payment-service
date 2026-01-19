package com.payment.service.model.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorMessage {

    USER_ID_NOT_FOUND("User with id: %s not found"),
    ;

    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
