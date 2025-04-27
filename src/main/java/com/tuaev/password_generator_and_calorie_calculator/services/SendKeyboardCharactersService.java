package com.tuaev.password_generator_and_calorie_calculator.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public interface SendKeyboardCharactersService {
    ReplyKeyboardMarkup getReplyKeyboardMarkupCharacters();
    KeyboardRow getKeyboardButtonsCharacters();
}
