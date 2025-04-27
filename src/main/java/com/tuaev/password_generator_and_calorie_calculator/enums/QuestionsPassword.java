package com.tuaev.password_generator_and_calorie_calculator.enums;

public enum QuestionsPassword {
    CHARACTERS("Какие символы ты хочешь видеть у себя в пароле?\nМожно выбрать несколько вариантов"),
    LENGTH("Какой длины ты хочешь пароль? Максимальное допустимое количество символов: 16");

    private final String text;


    QuestionsPassword(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
