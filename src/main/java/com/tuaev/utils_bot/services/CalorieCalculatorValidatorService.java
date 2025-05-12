package com.tuaev.utils_bot.services;

public interface CalorieCalculatorValidatorService {

    int checkValidDataOnQuestionsAboutCalories(int iterator, String userId, String text);

    int checkValidDataWeight(int iterator, String userId, String text);

    int checkValidDataHeight(int iterator, String userId, String text);

    int checkValidDataAge(int iterator, String userId, String text);

    int checkValidDataSex(int iterator, String userId, String text);

    int checkValidDataActivity(int iterator, String userId, String text);
}
