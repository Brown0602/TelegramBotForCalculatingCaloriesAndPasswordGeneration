package com.tuaev.password_generator_and_calorie_calculator.enums;

public enum Floor {

    MAN("Мужчина"),
    WOMAN("Женщина");

    private final String text;

    Floor(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
