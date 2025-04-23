package com.tuaev.password_generator_and_calorie_calculator.bot;

import com.tuaev.password_generator_and_calorie_calculator.configuration_properties_bot.ConfigurationPropertiesBot;
import com.tuaev.password_generator_and_calorie_calculator.enums.Activity;
import com.tuaev.password_generator_and_calorie_calculator.enums.Commands;
import com.tuaev.password_generator_and_calorie_calculator.enums.Sex;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsCalories;
import com.tuaev.password_generator_and_calorie_calculator.exeception.StringMaxLengthExceededException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.logging.Logger;

@Component
public class PasswordGeneratorAndCalorieCalculatorBot extends TelegramLongPollingBot {

    private final Logger logger = Logger.getLogger(PasswordGeneratorAndCalorieCalculatorBot.class.getName());
    private final ConfigurationPropertiesBot configurationPropertiesBot;
    private final Map<String, Commands> userCommandStates = new HashMap<>();
    private final Map<String, Integer> iteratorUserById = new HashMap<>();
    private final QuestionsCalories[] questionsCalories = QuestionsCalories.values();
    private final Activity[] activities = Activity.values();
    private final Map<String, List<Map<QuestionsCalories, String>>> responsesUserOnQuestionsCalories = new HashMap<>();

    public PasswordGeneratorAndCalorieCalculatorBot(ConfigurationPropertiesBot configurationPropertiesBot) {
        this.configurationPropertiesBot = configurationPropertiesBot;
    }

    @Override
    public String getBotUsername() {
        return configurationPropertiesBot.getUsername();
    }

    @Override
    public String getBotToken() {
        return configurationPropertiesBot.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long start = System.currentTimeMillis();
        Message message = update.getMessage();
        User user = message.getFrom();
        String userId = String.valueOf(user.getId());
        boolean isCommand = checkMessageOnCommand(message, userId);
        checkStateUser(userId, message);
        long end = System.currentTimeMillis();
        String result = String.format("Выполнено за %dms", end - start);
        logger.info(result);
    }

    public boolean checkMessageOnCommand(Message message, String userId) {
        Commands state = userCommandStates.get(userId);
        if (state != null) {
            return true;
        }
        boolean isCommand = message.isCommand();
        String textMessage = message.getText();
        if (isCommand && textMessage.equals(Commands.START.getText())) {
            handleCommandStart(Commands.START.getInfo(), userId);
            return true;
        }
        if (isCommand && textMessage.equals(Commands.CALORIES.getText())) {
            handleCommandCalories(Commands.CALORIES.getInfo(), userId, deleteKeyboard());
            userCommandStates.put(userId, Commands.CALORIES);
            iteratorUserById.put(userId, 0);
            return true;
        }
        if (isCommand && textMessage.equals(Commands.PASSWORD.getText())){
            handleCommandPassword(Commands.PASSWORD.getInfo(), userId, deleteKeyboard());
            return true;
        }
        handleNoCommand(userId);
        return false;
    }

    public int checkValidData(int iterator, String userId, Message message) {
        if (iterator > 0 && iterator <= questionsCalories.length) {
            if (questionsCalories[iterator - 1] == QuestionsCalories.WEIGHT) {
                iterator = checkValidDataWeight(iterator, userId, message);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.HEIGHT) {
                iterator = checkValidDataHeight(iterator, userId, message);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.AGE) {
                iterator = checkValidDataAge(iterator, userId, message);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.SEX) {
                iterator = checkValidDataSex(iterator, userId, message);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.ACTIVITY) {
                iterator = checkValidDataActivity(iterator, userId, message);
            }
        }
        return iterator;
    }

    public int checkValidDataActivity(int iterator, String userId, Message message) {
        boolean isValidDataActivity = Arrays.stream(activities).anyMatch(activity ->
                activity.getText().equals(message.getText()));
        if (isValidDataActivity) {
            addedResponseOnQuestion(userId, message, iterator);
        } else {
            String text = """
                    Выберите ответ из предложенных
                    """;
            sendKeyboardActivities(text, userId, getKeyboardActivity());
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataSex(int iterator, String userId, Message message) {
        if (message.getText().equals(Sex.MAN.getText()) || message.getText().equals(Sex.WOMAN.getText())) {
            addedResponseOnQuestion(userId, message, iterator);
        } else {
            String text = """
                    Выберите ответ из предложенных
                    """;
            sendMessage(text, userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataAge(int iterator, String userId, Message message) {
        try {
            int min = 1;
            int max = 100;
            int age;
            if (message.getText().length() > 3) {
                throw new StringMaxLengthExceededException("Длина значения возраста не должна превышать 3-х символов");
            } else {
                age = Integer.parseInt(message.getText());
            }
            boolean isValidAge = age >= min && age <= max;
            if (isValidAge) {
                addedResponseOnQuestion(userId, message, iterator);
            } else {
                String text = """
                        Возраст не может быть меньше 1 или больше 100
                        """;
                sendMessage(text, userId);
                iterator--;
                iteratorUserById.replace(userId, iterator);
            }
        } catch (NumberFormatException e) {
            String text = """
                    Значение возраста должно быть целым числом
                    """;
            sendMessage(text, userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        } catch (StringMaxLengthExceededException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataHeight(int iterator, String userId, Message message) {
        try {
            int min = 1;
            int max = 250;
            int height;
            if (message.getText().length() > 3) {
                throw new StringMaxLengthExceededException("Длина значения роста не должна превышать 3-х символов");
            } else {
                height = Integer.parseInt(message.getText());
            }
            boolean isValidHeight = height >= min && height <= max;
            if (isValidHeight) {
                addedResponseOnQuestion(userId, message, iterator);
            } else {
                String text = """
                        Рост не может быть меньше 1 или больше 250
                        """;
                sendMessage(text, userId);
                iterator--;
                iteratorUserById.replace(userId, iterator);
            }
        } catch (NumberFormatException e) {
            String text = """
                    Значение роста должно быть целым числом
                    """;
            sendMessage(text, userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        } catch (StringMaxLengthExceededException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataWeight(int iterator, String userId, Message message) {
        try {
            int min = 1;
            int max = 500;
            int weight;
            if (message.getText().length() > 3) {
                throw new StringMaxLengthExceededException("Длина значения веса не должна превышать 3-х символов");
            } else {
                weight = Integer.parseInt(message.getText());
            }
            boolean isValidWeight = weight >= min && weight <= max;
            if (isValidWeight) {
                addedResponseOnQuestion(userId, message, iterator);
            } else {
                String text = """
                        Вес не может быть меньше 1 или больше 500
                        """;
                sendMessage(text, userId);
                iterator--;
                iteratorUserById.replace(userId, iterator);
            }
        } catch (NumberFormatException e) {
            String text = """
                    Значение веса должно быть целым числом
                    """;
            sendMessage(text, userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        } catch (StringMaxLengthExceededException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public void checkStateUser(String userId, Message message) {
        if (userCommandStates.get(userId) == Commands.CALORIES) {
            int iterator = iteratorUserById.get(userId);
            iterator = checkValidData(iterator, userId, message);
            if (iterator < questionsCalories.length) {
                if (questionsCalories[iterator] == QuestionsCalories.WEIGHT || questionsCalories[iterator] == QuestionsCalories.HEIGHT || questionsCalories[iterator] == QuestionsCalories.AGE) {
                    sendMessage(questionsCalories[iterator].getText(), userId);
                    iterator++;
                    iteratorUserById.replace(userId, iterator);
                } else if (questionsCalories[iterator] == QuestionsCalories.SEX) {
                    sendKeyboardSex(questionsCalories[iterator].getText(), userId, getReplyKeyboardMarkupSex());
                    iterator++;
                    iteratorUserById.replace(userId, iterator);
                } else if (questionsCalories[iterator] == QuestionsCalories.ACTIVITY) {
                    String text = getActivities();
                    sendMessage(questionsCalories[iterator].getText(), userId);
                    sendKeyboardActivities(text, userId, getKeyboardActivity());
                    iterator++;
                    iteratorUserById.replace(userId, iterator);
                }
            }
            if (responsesUserOnQuestionsCalories.get(userId) != null && responsesUserOnQuestionsCalories.get(userId).size() == questionsCalories.length) {
                finalizeCalorieCalculationAndSendResult(userId);
            }
        }

        if (userCommandStates.get(userId) == Commands.PASSWORD) {

        }
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

    public void finalizeCalorieCalculationAndSendResult(String userId) {
        int weight = Integer.parseInt(responsesUserOnQuestionsCalories.get(userId).get(0).get(QuestionsCalories.WEIGHT));
        int height = Integer.parseInt(responsesUserOnQuestionsCalories.get(userId).get(1).get(QuestionsCalories.HEIGHT));
        int age = Integer.parseInt(responsesUserOnQuestionsCalories.get(userId).get(2).get(QuestionsCalories.AGE));
        String sex = responsesUserOnQuestionsCalories.get(userId).get(3).get(QuestionsCalories.SEX);
        String activity = responsesUserOnQuestionsCalories.get(userId).get(4).get(QuestionsCalories.ACTIVITY);
        String result = String.format("""
                С учетом введенных вами данных, ваш результат будет равен %d ккал в день для поддержания веса.
                Для сброса веса убавьте 500 ккал, а для набора добавьте 500 ккал.
                """, calorieCalculator(weight, height, age, sex, activity));
        handleCommandCalories(result, userId, deleteKeyboard());
        userCommandStates.remove(userId);
        iteratorUserById.remove(userId);
        responsesUserOnQuestionsCalories.remove(userId);
    }

    public void addedResponseOnQuestion(String userId, Message message, int count) {
        if (count > 0 && count <= questionsCalories.length) {
            Map<QuestionsCalories, String> questionAndResponse = new EnumMap<>(QuestionsCalories.class);
            questionAndResponse.put(questionsCalories[count - 1], message.getText());
            List<Map<QuestionsCalories, String>> list;
            if (responsesUserOnQuestionsCalories.get(userId) == null) {
                list = new ArrayList<>();
                list.add(questionAndResponse);
                responsesUserOnQuestionsCalories.put(userId, list);
            } else {
                list = responsesUserOnQuestionsCalories.get(userId);
                list.add(questionAndResponse);
                responsesUserOnQuestionsCalories.replace(userId, list);
            }
        }
    }

    public ReplyKeyboardRemove deleteKeyboard() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setSelective(false);
        keyboardRemove.setRemoveKeyboard(true);
        return keyboardRemove;
    }

    public ReplyKeyboardMarkup getKeyboardActivity() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton noActivity = new KeyboardButton(Activity.NO_ACTIVITY.getText());
        KeyboardButton easy = new KeyboardButton(Activity.EASY.getText());
        KeyboardButton average = new KeyboardButton(Activity.AVERAGE.getText());
        KeyboardButton high = new KeyboardButton(Activity.HIGH.getText());
        KeyboardButton veryHigh = new KeyboardButton(Activity.VERY_HIGH.getText());
        row.add(noActivity);
        row.add(easy);
        row.add(average);
        row.add(high);
        row.add(veryHigh);
        rows.add(row);
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    public ReplyKeyboardMarkup getReplyKeyboardMarkupSex() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton(Sex.MAN.getText());
        KeyboardButton button2 = new KeyboardButton(Sex.WOMAN.getText());
        row.add(button1);
        row.add(button2);
        rows.add(row);
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

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

    public void getError(Exception e) {
        logger.info("Произошла ошибка: " + e.getMessage());
    }

    public void handleNoCommand(String userId) {
        try {
            String text = "Для взаимодейсвия с ботом воспользуйтесь командным меню";
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    public void handleCommandStart(String text, String userId) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    public void sendMessage(String text, String userId) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    public void handleCommandCalories(String text, String userId, ReplyKeyboardRemove keyboardRemove) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .replyMarkup(keyboardRemove)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    public void handleCommandPassword(String text, String userId, ReplyKeyboardRemove keyboardRemove) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .replyMarkup(keyboardRemove)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    public void sendKeyboardActivities(String text, String userId, ReplyKeyboardMarkup keyboardCreate) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .replyMarkup(keyboardCreate)
                    .parseMode("HTML")
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    public void sendKeyboardSex(String text, String userId, ReplyKeyboardMarkup keyboardCreate) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .replyMarkup(keyboardCreate)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }
}
