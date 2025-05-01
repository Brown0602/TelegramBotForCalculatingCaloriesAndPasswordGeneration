package com.tuaev.utils_bot.enums;

public enum QuestionsPassword {
    CHARACTERS("""
            Какие символы ты хочешь видеть у себя в пароле?
            1.Целые числа
            2.Специальные символы
            3.Строчные буквы
            4.Заглавные буквы
            Можно выбрать до 4 вариантов"""),
    LENGTH("Какой длины ты хочешь пароль? Максимальное допустимое количество символов: 16");

    private final String text;


    QuestionsPassword(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
