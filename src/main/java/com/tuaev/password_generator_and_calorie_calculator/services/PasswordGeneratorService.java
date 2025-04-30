package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.Chars;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface PasswordGeneratorService{

    InlineKeyboardMarkup getInlineKeyboardGenerationPassword();
    InlineKeyboardMarkup getInlineKeyboardCharacters();
    List<Chars> getChars();
    String getTextQuestionPasswordByIterator(int iterator);
    QuestionsPassword getQuestionPasswordByIterator(int iterator);
    boolean addResponseOnQuestion(String userId, String text, int iterator);
    int getLengthQuestionsPassword();
}
