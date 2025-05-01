package com.tuaev.utils_bot.enums;

public enum Chars {
    NUMBERS("Целые числа", "1234567890"),
    SPECIAL_CHARACTERS("Специальные символы", "[]!\"№;'%:?*()/.,_-+=@#$^&"),
    UPPER_CASE("Заглавные буквы", "QWERTYUIOPASDFGHJKLZXCVBNM"),
    LOWER_CASE("Строчные буквы", "qwertyuiopasdfghjklzxcvbnm");

    private final String info;
    private final String value;


    Chars(String info, String value) {
        this.info = info;
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public String getValue() {
        return value;
    }
}
