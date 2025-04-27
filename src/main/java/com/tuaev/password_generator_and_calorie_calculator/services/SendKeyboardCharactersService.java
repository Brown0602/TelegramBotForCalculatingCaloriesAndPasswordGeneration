package com.tuaev.password_generator_and_calorie_calculator.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public interface SendKeyboardCharactersService {
    InlineKeyboardMarkup getInlineKeyboardMarkupCharacters();
    KeyboardRow getKeyboardButtonsCharacters();
}
