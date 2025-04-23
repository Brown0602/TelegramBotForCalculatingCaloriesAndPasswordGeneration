package com.tuaev.password_generator_and_calorie_calculator.bot;

import com.tuaev.password_generator_and_calorie_calculator.configuration_properties_bot.ConfigurationPropertiesBot;
import com.tuaev.password_generator_and_calorie_calculator.enums.*;
import com.tuaev.password_generator_and_calorie_calculator.exeception.NotValidDataException;
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
    private final QuestionsPassword[] questionsPasswords = QuestionsPassword.values();
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
        String text = message.getText();
        boolean isCommand = message.isCommand();
        checkMessageOnCommandFromUser(isCommand, text, userId);
        processingCommand(text, userId);
        long end = System.currentTimeMillis();
        String result = String.format("Выполнено за %dms", end - start);
        logger.info(result);
    }

    public void checkMessageOnCommandFromUser(boolean isCommand, String text, String userId) {
        if (userCommandStates.get(userId) != null) {
            return;
        }
        if (isCommand && text.equals(Commands.START.getText())) {
            handleCommandStart(Commands.START.getInfo(), userId);
        } else if (isCommand && text.equals(Commands.CALORIES.getText())) {
            userCommandStates.put(userId, Commands.CALORIES);
            iteratorUserById.put(userId, 0);
            handleCommandCalories(Commands.CALORIES.getInfo(), userId, deleteKeyboard());
        } else if (isCommand && text.equals(Commands.PASSWORD.getText())) {
            userCommandStates.put(userId, Commands.PASSWORD);
            iteratorUserById.put(userId, 0);
            handleCommandPassword(Commands.PASSWORD.getInfo(), userId, deleteKeyboard());
        } else {
            handleNoCommand(userId);
        }
    }

    public int checkValidData(int iterator, String userId, String text) {
        if (iterator > 0 && iterator <= questionsCalories.length) {
            if (questionsCalories[iterator - 1] == QuestionsCalories.WEIGHT) {
                iterator = checkValidDataWeight(iterator, userId, text);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.HEIGHT) {
                iterator = checkValidDataHeight(iterator, userId, text);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.AGE) {
                iterator = checkValidDataAge(iterator, userId, text);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.SEX) {
                iterator = checkValidDataSex(iterator, userId, text);
            } else if (questionsCalories[iterator - 1] == QuestionsCalories.ACTIVITY) {
                iterator = checkValidDataActivity(iterator, userId, text);
            }
        }
        return iterator;
    }

    public int checkValidDataActivity(int iterator, String userId, String text) {
        boolean isValidDataActivity = Arrays.stream(activities).anyMatch(activity ->
                activity.getText().equals(text));
        try {
            if (isValidDataActivity) {
                addResponseOnQuestion(userId, text, iterator);
            } else {
                throw new NotValidDataException("Нет такого варианта ответа\nВыберите ответ из предложенных");
            }
        } catch (NotValidDataException e) {
            sendKeyboardActivities(e.getMessage(), userId, getKeyboardActivity());
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataSex(int iterator, String userId, String text) {
        try {
            if (text.equals(Sex.MAN.getText()) || text.equals(Sex.WOMAN.getText())) {
                addResponseOnQuestion(userId, text, iterator);
            } else {
                throw new NotValidDataException("Нет такого варианта ответа\nВыберите ответ из предложенных");
            }
        } catch (NotValidDataException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataAge(int iterator, String userId, String text) {
        try {
            if (text.length() <= 3 && text.matches("\\d+") && Integer.parseInt(text) > 0 && Integer.parseInt(text) < 100) {
                addResponseOnQuestion(userId, text, iterator);
            } else {
                throw new NotValidDataException("Возраст должен быть больше 0 и меньше 100, а так же содержать только целые числа");
            }
        } catch (NotValidDataException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataHeight(int iterator, String userId, String text) {
        try {
            if (text.length() <= 3 && text.matches("\\d+") && Integer.parseInt(text) > 0 && Integer.parseInt(text) < 250) {
                addResponseOnQuestion(userId, text, iterator);
            } else {
                throw new NotValidDataException("Рост должен быть больше 0 и меньше 250, а так же содержать только целые числа");
            }
        } catch (NotValidDataException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public int checkValidDataWeight(int iterator, String userId, String text) {
        try {
            if (text.length() <= 3 && text.matches("\\d+") && Integer.parseInt(text) > 0 && Integer.parseInt(text) < 500) {
                addResponseOnQuestion(userId, text, iterator);
            } else {
                throw new NotValidDataException("Вес должен быть больше 0 и меньше 500, а так же содержать только целые числа");
            }
        } catch (NotValidDataException e) {
            sendMessage(e.getMessage(), userId);
            iterator--;
            iteratorUserById.replace(userId, iterator);
        }
        return iterator;
    }

    public void processingCommand(String text, String userId) {
        Commands state = userCommandStates.get(userId);
        int iterator = 0;
        if (state != null) {
            iterator = iteratorUserById.get(userId);
        }
        if (state == Commands.CALORIES) {
            iterator = checkValidData(iterator, userId, text);
            sendingQuestions(userId, iterator);
            if (responsesUserOnQuestionsCalories.get(userId) != null && responsesUserOnQuestionsCalories.get(userId).size() == questionsCalories.length) {
                finalizeCalorieCalculationAndSendResult(userId);
            }
        }
        if (userCommandStates.get(userId) == Commands.PASSWORD) {
            if (questionsPasswords[iterator] == QuestionsPassword.CHARACTERS) {
                sendKeyboardCharacters(QuestionsPassword.CHARACTERS.getText(), userId, getReplyKeyboardMarkupCharacters());
            }
            sendMessage(QuestionsPassword.LENGTH.getText(), userId);
        }
    }

    public void sendingQuestions(String userId, int iterator) {
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

    public void addResponseOnQuestion(String userId, String text, int count) {
        if (count > 0 && count <= questionsCalories.length) {
            Map<QuestionsCalories, String> questionAndResponse = new EnumMap<>(QuestionsCalories.class);
            questionAndResponse.put(questionsCalories[count - 1], text);
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

    public ReplyKeyboardMarkup getReplyKeyboardMarkupCharacters() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow keyboardRow = getKeyboardButtons();
        rows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public KeyboardRow getKeyboardButtons() {
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton(Chars.NUMBERS.getInfo());
        KeyboardButton button2 = new KeyboardButton(Chars.SPECIAL_CHARACTERS.getInfo());
        KeyboardButton button3 = new KeyboardButton(Chars.UPPER_CASE.getInfo());
        KeyboardButton button4 = new KeyboardButton(Chars.LOWER_CASE.getInfo());
        keyboardRow.add(button1);
        keyboardRow.add(button2);
        keyboardRow.add(button3);
        keyboardRow.add(button4);
        return keyboardRow;
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

    public void sendKeyboardCharacters(String text, String userId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .replyMarkup(replyKeyboardMarkup)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }
}
