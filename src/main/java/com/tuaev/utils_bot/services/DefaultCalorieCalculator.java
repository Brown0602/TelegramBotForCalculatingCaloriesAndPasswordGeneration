package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.Activity;
import com.tuaev.utils_bot.enums.QuestionsCalories;
import com.tuaev.utils_bot.enums.Sex;
import com.tuaev.utils_bot.exeception.NotValidDataException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultCalorieCalculator implements CalorieCalculatorService {

    private final QuestionsCalories[] questionsCalories = QuestionsCalories.values();
    private final Activity[] activities = Activity.values();
    private final Map<String, Map<QuestionsCalories, String>> responsesUsersOnQuestionsAboutCalories = new HashMap<>();

    public QuestionsCalories[] getQuestionsCalories() {
        return questionsCalories;
    }

    public Map<String, Map<QuestionsCalories, String>> getResponsesUsersOnQuestionsAboutCalories() {
        return responsesUsersOnQuestionsAboutCalories;
    }

    @Override
    public int checkValidDataOnQuestionsAboutCalories(int iterator, String userId, String text) {
        if (iterator > 0 && iterator <= questionsCalories.length) {
            if (questionsCalories[iterator - 1] == QuestionsCalories.WEIGHT) {
                return checkValidDataWeight(iterator, userId, text);
            }
            if (questionsCalories[iterator - 1] == QuestionsCalories.HEIGHT) {
                return checkValidDataHeight(iterator, userId, text);
            }
            if (questionsCalories[iterator - 1] == QuestionsCalories.AGE) {
                return checkValidDataAge(iterator, userId, text);
            }
            if (questionsCalories[iterator - 1] == QuestionsCalories.SEX) {
                return checkValidDataSex(iterator, userId, text);
            }
            if (questionsCalories[iterator - 1] == QuestionsCalories.ACTIVITY) {
                return checkValidDataActivity(iterator, userId, text);
            }
        }
        return 0;
    }

    @Override
    public int checkValidDataWeight(int iterator, String userId, String text) {
        if (text.length() <= 3 && text.matches("\\d+") && Integer.parseInt(text) > 0 && Integer.parseInt(text) < 500) {
            addResponseOnQuestionAboutCalories(userId, text, iterator);
        } else {
            throw new NotValidDataException("Вес должен быть больше 0 и меньше 500, а так же содержать только целые числа");
        }
        return iterator;
    }

    @Override
    public int checkValidDataHeight(int iterator, String userId, String text) {
        if (text.length() <= 3 && text.matches("\\d+") && Integer.parseInt(text) > 0 && Integer.parseInt(text) < 250) {
            addResponseOnQuestionAboutCalories(userId, text, iterator);
        } else {
            throw new NotValidDataException("Рост должен быть больше 0 и меньше 250, а так же содержать только целые числа");
        }
        return iterator;
    }

    @Override
    public int checkValidDataAge(int iterator, String userId, String text) {
        if (text.length() <= 3 && text.matches("\\d+") && Integer.parseInt(text) > 0 && Integer.parseInt(text) < 100) {
            addResponseOnQuestionAboutCalories(userId, text, iterator);
        } else {
            throw new NotValidDataException("Возраст должен быть больше 0 и меньше 100, а так же содержать только целые числа");
        }
        return iterator;
    }

    @Override
    public int checkValidDataSex(int iterator, String userId, String text) {
        if (text.equals(Sex.MAN.getText()) || text.equals(Sex.WOMAN.getText())) {
            addResponseOnQuestionAboutCalories(userId, text, iterator);
        } else {
            throw new NotValidDataException("Нет такого варианта ответа\nВыберите ответ из предложенных");
        }
        return iterator;
    }

    @Override
    public int checkValidDataActivity(int iterator, String userId, String text) {
        boolean isValidDataActivity = Arrays.stream(activities).anyMatch(activity ->
                activity.getText().equals(text));
        if (isValidDataActivity) {
            addResponseOnQuestionAboutCalories(userId, text, iterator);
        } else {
            throw new NotValidDataException("Нет такого варианта ответа\nВыберите ответ из предложенных");
        }
        return iterator;
    }

    @Override
    public void addResponseOnQuestionAboutCalories(String userId, String text, int iterator) {
        if (iterator > 0 && iterator <= questionsCalories.length) {
            Map<QuestionsCalories, String> responseOnQuestion;
            if (responsesUsersOnQuestionsAboutCalories.get(userId) == null) {
                responseOnQuestion = new EnumMap<>(QuestionsCalories.class);
                responseOnQuestion.put(questionsCalories[iterator - 1], text);
                responsesUsersOnQuestionsAboutCalories.put(userId, responseOnQuestion);
            } else {
                responseOnQuestion = responsesUsersOnQuestionsAboutCalories.get(userId);
                responseOnQuestion.put(questionsCalories[iterator - 1], text);
                responsesUsersOnQuestionsAboutCalories.replace(userId, responseOnQuestion);
            }
        }
    }

    @Override
    public String finalizeCalorieCalculation(String userId) {
        int weight = Integer.parseInt(responsesUsersOnQuestionsAboutCalories.get(userId).get(QuestionsCalories.WEIGHT));
        int height = Integer.parseInt(responsesUsersOnQuestionsAboutCalories.get(userId).get(QuestionsCalories.HEIGHT));
        int age = Integer.parseInt(responsesUsersOnQuestionsAboutCalories.get(userId).get(QuestionsCalories.AGE));
        String sex = responsesUsersOnQuestionsAboutCalories.get(userId).get(QuestionsCalories.SEX);
        String activity = responsesUsersOnQuestionsAboutCalories.get(userId).get(QuestionsCalories.ACTIVITY);
        String result = String.format("""
                С учетом введенных вами данных, ваш результат будет равен %d ккал в день для поддержания веса.
                Для сброса веса убавьте 500 ккал, а для набора добавьте 500 ккал.
                """, calorieCalculator(weight, height, age, sex, activity));
        responsesUsersOnQuestionsAboutCalories.remove(userId);
        return result;
    }

    @Override
    public int calorieCalculator(int weight, int height, int age, String floor, String activity) {
        double result;
        Optional<Activity> optionalActivity = Arrays.stream(activities)
                .filter(a -> a.getText().equals(activity))
                .findFirst();
        Activity act = optionalActivity.orElseThrow();
        if (floor.equals(Sex.MAN.getText())) {
            result = 10 * weight + 6.25 * height - 5 * age + 5;
            return (int) Math.round(result * act.getRatio());
        }
        result = 10 * weight + 6.25 * height - 5 * age - 161;
        return (int) Math.round(result * act.getRatio());
    }

    @Override
    public boolean isFullnessResponsesUsersOnQuestionsAboutCalories(String userId) {
        return responsesUsersOnQuestionsAboutCalories.get(userId) != null
                && responsesUsersOnQuestionsAboutCalories.get(userId).size() == questionsCalories.length;
    }

    @Override
    public QuestionsCalories getQuestionAboutCaloriesByIterator(int iterator) {
        return iterator < questionsCalories.length ? questionsCalories[iterator] : null;
    }

    public String getActivities() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Activity activity : activities) {
            stringBuilder.append("<b>")
                    .append(activity.getText())
                    .append(":")
                    .append("</b>")
                    .append("\n")
                    .append(activity.getInfo())
                    .append("\n");
        }
        return String.valueOf(stringBuilder);
    }
}
