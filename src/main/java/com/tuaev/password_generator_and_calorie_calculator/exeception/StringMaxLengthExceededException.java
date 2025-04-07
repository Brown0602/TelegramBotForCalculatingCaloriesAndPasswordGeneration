package com.tuaev.password_generator_and_calorie_calculator.exeception;

public class StringMaxLengthExceededException extends RuntimeException{
    public StringMaxLengthExceededException(String message) {
        super(message);
    }

    public StringMaxLengthExceededException() {
        super();
    }
}
