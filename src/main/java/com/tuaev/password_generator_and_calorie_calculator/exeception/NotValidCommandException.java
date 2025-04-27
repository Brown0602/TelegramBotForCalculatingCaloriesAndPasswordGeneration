package com.tuaev.password_generator_and_calorie_calculator.exeception;

public class NotValidCommandException extends RuntimeException{
    public NotValidCommandException() {
        super();
    }

    public NotValidCommandException(String message) {
        super(message);
    }
}
