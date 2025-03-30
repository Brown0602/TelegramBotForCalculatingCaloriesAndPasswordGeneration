package com.tuaev.password_generator_and_calorie_calculator.enums;

public enum Commands {

    START("/start", """
                    Привет!
                    Я бот, который рассчитает тебе суточную норму твоих калорий и поможет сгенерировать тебе пароль:)
                    Для взаимодействия со мной воспользуйся командным меню
                    """),
    CALORIES("/calories", """
                    Отлично! Давай рассчитаем твою суточную норму калорий
                    Рассчет производится по формуле Миффлина - Сан-Жеора
                    """),
    PASSWORD("/password", """
            Отлично! Давай придумаем тебе классный пароль!:)
            """);

    private final String text;
    private final String info;

    Commands(String text, String info) {
        this.text = text;
        this.info = info;
    }

    public String getText() {
        return text;
    }

    public String getInfo() {
        return info;
    }
}
