package com.tuaev.utils_bot.exeception;

public class NotValidDataException extends RuntimeException{

    public NotValidDataException() {
        super();
    }

    public NotValidDataException(String message) {
        super(message);
    }
}
