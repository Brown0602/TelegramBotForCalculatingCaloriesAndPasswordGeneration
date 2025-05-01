package com.tuaev.utils_bot.enums;

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
