package com.tuaev.utils_bot.exeception;

public class NotValidCommandException extends RuntimeException{
    public NotValidCommandException() {
        super();
    }

    public NotValidCommandException(String message) {
        super(message);
    }
}
