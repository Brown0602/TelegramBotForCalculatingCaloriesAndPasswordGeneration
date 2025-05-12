package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.QuestionsCalories;

public interface CalorieCalculatorService extends CalorieCalculatorValidatorService{

    void addResponseOnQuestionAboutCalories(String userId, String text, int iterator);

    String finalizeCalorieCalculation(String userId);

    int calorieCalculator(int weight, int height, int age, String floor, String activity);

    boolean isFullnessResponsesUsersOnQuestionsAboutCalories(String userId);

    QuestionsCalories getQuestionAboutCaloriesByIterator(int iterator);

    String getActivities();
}
