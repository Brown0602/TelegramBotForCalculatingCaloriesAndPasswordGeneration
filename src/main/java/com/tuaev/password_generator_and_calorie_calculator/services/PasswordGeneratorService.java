package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;

public interface PasswordGeneratorService extends SendKeyboardCharactersService{

    String getTextQuestionPasswordByIterator(int iterator);
    QuestionsPassword getQuestionPasswordByIterator(int iterator);
    void addResponseOnQuestion(String userId, String text, int iterator);
    int getLengthQuestionsPassword();
}
