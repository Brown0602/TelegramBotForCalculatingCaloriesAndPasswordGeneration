package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.Chars;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public interface PasswordGeneratorService{

    InlineKeyboardMarkup getInlineKeyboardCharacters();
    List<List<InlineKeyboardButton>> getInlineButtonsCharacters();
    List<Chars> getChars();
    String getTextQuestionPasswordByIterator(int iterator);
    QuestionsPassword getQuestionPasswordByIterator(int iterator);
    boolean addResponseOnQuestion(String userId, String text, int iterator);
    int getLengthQuestionsPassword();
}
