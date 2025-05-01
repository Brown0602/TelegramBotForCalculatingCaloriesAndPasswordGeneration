package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.QuestionsPassword;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface PasswordGeneratorService {

    boolean isInlineButtonCharacter(String text);

    int getLengthListCharacters();

    int getLengthListOnQuestionAboutCharactersByUserId(String userId);

    InlineKeyboardMarkup getInlineKeyboardNextQuestion();

    InlineKeyboardMarkup getInlineKeyboardCharacters();

    String getTextQuestionPasswordByIterator(int iterator);

    QuestionsPassword getQuestionPasswordByIterator(int iterator);

    void addResponseOnQuestionAboutPassword(String userId, String text, QuestionsPassword question);

    int getLengthQuestionsPassword();
}
