package com.tuaev.password_generator_and_calorie_calculator.enums;

public enum Activity {

    NO_ACTIVITY(1.4, "Очень низкая", "Сидячий образ жизни(женщины и мужчины, работающие там, где не требуются физические усилия)"),
    EASY(1.6, "Низкая", "Низкая физическая активность(женщины и мужчины, занятые легким физическим трудом - водители, швеи, врачи, продавцы)"),
    AVERAGE(1.9, "Средняя", "Средняя активность(женщины и мужчины, работники средней тяжести труда - садовники, буровики, станочники)"),
    HIGH(2.2, "Высокая", "Очень высокая физическая активность(женщины и мужчины на позициях строительных рабочих, грузчиков, работников лесного, сельского хозяйства)"),
    VERY_HIGH(2.5, "Очень высокая", "Очень тяжелый физический труд(мужчины - шахтеры, бетонщики, каменщики, спортсмены в тренировочный период)");

    private final double ratio;
    private final String text;
    private final String info;

    Activity(double ratio, String text, String info) {
        this.ratio = ratio;
        this.text = text;
        this.info = info;
    }

    public double getRatio() {
        return ratio;
    }

    public String getText() {
        return text;
    }

    public String getInfo() {
        return info;
    }
}
