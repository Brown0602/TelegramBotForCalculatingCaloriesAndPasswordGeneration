package com.tuaev.utils_bot.enums;

public enum CallbackResponse {
    NEXT_QUESTION("Следующий вопрос");

    private final String text;

    CallbackResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
