package com.tuaev.password_generator_and_calorie_calculator.exeception;

public class NotValidDataException extends RuntimeException{
    public NotValidDataException(String message) {
        super(message);
    }

    public NotValidDataException() {
        super();
    }
}
