package com.tuaev.password_generator_and_calorie_calculator.enums;

public enum QuestionsCalories {
    WEIGHT("Какой у вас вес?"),
    HEIGHT("Какой ваш рост?"),
    AGE("Сколько вам лет?"),
    SEX("Вы мужчина или женщина?"),
    ACTIVITY("Выберите примерный уровень вашей физической активности:");

    private final String text;

    QuestionsCalories(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
