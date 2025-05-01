package com.tuaev.utils_bot.exeception;

public class NotUniqueResponseException extends RuntimeException{
    public NotUniqueResponseException() {
        super();
    }

    public NotUniqueResponseException(String message) {
        super(message);
    }
}
