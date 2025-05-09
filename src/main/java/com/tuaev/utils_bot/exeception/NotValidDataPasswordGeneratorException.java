package com.tuaev.utils_bot.exeception;

public class NotValidDataPasswordGeneratorException extends RuntimeException{

    public NotValidDataPasswordGeneratorException() {
        super();
    }

    public NotValidDataPasswordGeneratorException(String message) {
        super(message);
    }
}
