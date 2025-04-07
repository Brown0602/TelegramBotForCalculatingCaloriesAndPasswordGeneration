package com.tuaev.password_generator_and_calorie_calculator.enums;

public enum Sex {

    MAN("Мужчина"),
    WOMAN("Женщина");

    private final String text;

    Sex(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
