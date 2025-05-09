package com.tuaev.utils_bot.bot;

import com.tuaev.utils_bot.configuration_properties_bot.ConfigurationPropertiesBot;
import com.tuaev.utils_bot.enums.*;
import com.tuaev.utils_bot.exeception.NotUniqueResponseException;
import com.tuaev.utils_bot.exeception.NotValidCommandException;
import com.tuaev.utils_bot.exeception.NotValidDataException;
import com.tuaev.utils_bot.exeception.NotValidDataPasswordGeneratorException;
import com.tuaev.utils_bot.services.IteratorService;
import com.tuaev.utils_bot.services.PasswordGeneratorService;
import com.tuaev.utils_bot.services.SendMessageService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class UtilsBot extends TelegramLongPollingBot implements SendMessageService, IteratorService {

    private final Logger logger = Logger.getLogger(UtilsBot.class.getName());

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final Map<String, LocalDateTime> dateLastMessageUser = new ConcurrentHashMap<>();
    private final ConfigurationPropertiesBot configurationPropertiesBot;
    private final PasswordGeneratorService passwordGeneratorService;
    private final Map<String, Commands> userCommandStates = new ConcurrentHashMap<>();
    private final Map<String, Integer> iteratorUserById = new ConcurrentHashMap<>();
    private final QuestionsCalories[] questionsCalories = QuestionsCalories.values();
    private final Commands[] commands = Commands.values();
    private final Activity[] activities = Activity.values();
    private final Map<String, Map<QuestionsCalories, String>> responsesUserOnQuestionsCalories = new HashMap<>();

    public UtilsBot(ConfigurationPropertiesBot configurationPropertiesBot, PasswordGeneratorService passwordGeneratorService) {
        this.configurationPropertiesBot = configurationPropertiesBot;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @PostConstruct
    public void processingActivityUserWithBot(){
        try {
            checkActivityUser();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkActivityUser() throws ExecutionException, InterruptedException {
        final Runnable checkUser = new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, LocalDateTime> entry : dateLastMessageUser.entrySet()) {
                    String userId = entry.getKey();
                    String msg;
                    LocalDateTime timeLastInteractions = entry.getValue();
                    LocalDateTime now = LocalDateTime.now();
                    Duration duration = Duration.between(timeLastInteractions, now);
                    long seconds = duration.toSeconds();
                    msg = String.format("Пользователь с id: %s был активен %d секунд назад", userId, seconds);
                    logger.log(Level.INFO, msg);
                    if (seconds > 180) {
                        dateLastMessageUser.remove(userId);
                        userCommandStates.remove(userId);
                        iteratorUserById.remove(userId);
                        msg = String.format("Пользователь с id %s удален из кэша\nОн был неактивен более %d секунд", userId, seconds);
                        logger.log(Level.INFO, msg);
                        return;
                    }
                    return;
                }
                logger.log(Level.INFO, () -> "Нет активных пользователей");
            }
        };
        scheduledExecutorService.scheduleWithFixedDelay(checkUser, 60, 60, TimeUnit.SECONDS);
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
        String userId = null;
        String text = null;
        LocalDateTime timeLastInteractions = LocalDateTime.now();
        if (update.hasCallbackQuery()) {
            userId = String.valueOf(update.getCallbackQuery().getFrom().getId());
            text = update.getCallbackQuery().getData();
        }
        if (update.hasMessage()) {
            userId = String.valueOf(update.getMessage().getFrom().getId());
            text = update.getMessage().getText();
        }
        dateLastMessageUser.put(userId, timeLastInteractions);
        checkMessageOnCommandFromUser(text, userId);
        processingCommand(text, userId, update);
        long end = System.currentTimeMillis();
        String result = String.format("Выполнено за %dms", end - start);
        logger.info(result);
    }

    public void checkMessageOnCommandFromUser(String text, String userId) {
        boolean isCommand = Arrays.stream(commands).anyMatch(command -> command.getText().equals(text));
        if (userCommandStates.get(userId) == null && !isCommand) {
            try {
                throw new NotValidCommandException("Для взаимодействия с ботов воспользуйтесь командным меню");
            } catch (NotValidCommandException e) {
                sendMessage(e.getMessage(), userId);
            }
            return;
        }
        if (isCommand) {
            Optional<Commands> optionalCommand = Arrays.stream(commands)
                    .filter(command -> command.getText().equals(text))
                    .findFirst();
            if (optionalCommand.isPresent()) {
                Commands command = optionalCommand.get();
                switch (command) {
                    case START -> {
                        userCommandStates.remove(userId);
                        iteratorUserById.remove(userId);
                        sendMessage(Commands.START.getInfo(), userId);
                    }
                    case CALORIES -> {
                        userCommandStates.put(userId, Commands.CALORIES);
                        iteratorUserById.put(userId, 0);
                        sendMessageWithRemovedKeyboard(Commands.CALORIES.getInfo(), userId, deleteKeyboard());
                    }
                    case PASSWORD -> {
                        userCommandStates.put(userId, Commands.PASSWORD);
                        iteratorUserById.put(userId, 0);
                        passwordGeneratorService.deleteResponsesUserAboutQuestionsAboutPassword(userId);
                        sendMessageWithRemovedKeyboard(Commands.PASSWORD.getInfo(), userId, deleteKeyboard());
                    }
                }
            }
        }
    }

    public int checkValidData(int iterator, String userId, String text) {
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
            sendKeyboardWithHtmlTextSupport(e.getMessage(), userId, getKeyboardActivity());
            iterator = decrement(iterator);
            replaceIteratorByUserId(userId, iterator);
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
            iterator = decrement(iterator);
            replaceIteratorByUserId(userId, iterator);
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
            iterator = decrement(iterator);
            replaceIteratorByUserId(userId, iterator);
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
            iterator = decrement(iterator);
            replaceIteratorByUserId(userId, iterator);
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
            iterator = decrement(iterator);
            replaceIteratorByUserId(userId, iterator);
        }
        return iterator;
    }

    public void processingCommand(String text, String userId, Update update) {
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
            String textQuestion;
            QuestionsPassword question = null;
            boolean isCallbackQuery = update.hasCallbackQuery();
            if (iterator < passwordGeneratorService.getLengthQuestionsPassword()) {
                if (isCallbackQuery && text.equals(CallbackResponse.NEXT_QUESTION.getText())) {
                    iterator = increment(iterator);
                    replaceIteratorByUserId(userId, iterator);
                }
                textQuestion = passwordGeneratorService.getTextQuestionPasswordByIterator(iterator);
                question = passwordGeneratorService.getQuestionPasswordByIterator(iterator);
                if (!isCallbackQuery && question == QuestionsPassword.CHARACTERS) {
                    InlineKeyboardMarkup inlineKeyboardMarkup = passwordGeneratorService.getInlineKeyboardCharacters();
                    sendKeyboardWithoutAnyModSupport(textQuestion, userId, inlineKeyboardMarkup);
                    return;
                }
                if (isCallbackQuery && text.equals(CallbackResponse.NEXT_QUESTION.getText()) && question == QuestionsPassword.LENGTH) {
                    sendMessage(textQuestion, userId);
                    return;
                }
            }
            if (isCallbackQuery) {
                if (passwordGeneratorService.isInlineButtonCharacter(text)) {
                    try {
                        passwordGeneratorService.addResponseOnQuestionAboutPassword(userId, text, question);
                        InlineKeyboardMarkup nextQuestion = passwordGeneratorService.getInlineKeyboardNextQuestion();
                        sendKeyboardWithoutAnyModSupport("""
                                Запомнила\uD83D\uDE01
                                Если выбрал желаемые варианты, то нажми на кнопку "Следующий вопрос"
                                """, userId, nextQuestion);
                        return;
                    } catch (NotUniqueResponseException e) {
                        sendMessage(e.getMessage(), userId);
                        return;
                    }
                }
                if (passwordGeneratorService.isCallbackResponse(text)) {
                    if (text.equals(CallbackResponse.GENERATION_PASSWORD.getText())) {
                        String password = String.format("""
                                Сгенерировала тебе пароль\uD83D\uDE0A
                                %s
                                Если данный пароль тебе не понравился, то нажми на кнопку "%s"
                                """, passwordGeneratorService.generationPassword(userId), CallbackResponse.GENERATION_PASSWORD.getText());
                        sendKeyboardWithoutAnyModSupport(password, userId, passwordGeneratorService.getKeyboardGenerationPassword());
                        return;
                    }
                }
            }
            try {
                passwordGeneratorService.addResponseOnQuestionAboutPassword(userId, text, question);
            } catch (NotValidDataPasswordGeneratorException e) {
                sendMessage(e.getMessage(), userId);
                getError(e);
                return;
            }
            String password = String.format("""
                    Сгенерировала тебе пароль\uD83D\uDE0A
                    %s
                    Если данный пароль тебе не понравился, то нажми на кнопку "%s"
                    """, passwordGeneratorService.generationPassword(userId), CallbackResponse.GENERATION_PASSWORD.getText());
            sendKeyboardWithoutAnyModSupport(password, userId, passwordGeneratorService.getKeyboardGenerationPassword());
            //userCommandStates.remove(userId);
            //iteratorUserById.remove(userId);
        }
    }

    public void sendingQuestions(String userId, int iterator) {
        if (iterator < questionsCalories.length) {
            if (questionsCalories[iterator] == QuestionsCalories.WEIGHT || questionsCalories[iterator] == QuestionsCalories.HEIGHT || questionsCalories[iterator] == QuestionsCalories.AGE) {
                sendMessage(questionsCalories[iterator].getText(), userId);
                iterator = increment(iterator);
            } else if (questionsCalories[iterator] == QuestionsCalories.SEX) {
                sendKeyboardWithoutAnyModSupport(questionsCalories[iterator].getText(), userId, getReplyKeyboardMarkupSex());
                iterator = increment(iterator);
            } else if (questionsCalories[iterator] == QuestionsCalories.ACTIVITY) {
                String text = getActivities();
                sendMessage(questionsCalories[iterator].getText(), userId);
                sendKeyboardWithHtmlTextSupport(text, userId, getKeyboardActivity());
                iterator = increment(iterator);
            }
            replaceIteratorByUserId(userId, iterator);
        }
    }

    private void replaceIteratorByUserId(String userId, int iterator) {
        iteratorUserById.replace(userId, iterator);
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
        int weight = Integer.parseInt(responsesUserOnQuestionsCalories.get(userId).get(QuestionsCalories.WEIGHT));
        int height = Integer.parseInt(responsesUserOnQuestionsCalories.get(userId).get(QuestionsCalories.HEIGHT));
        int age = Integer.parseInt(responsesUserOnQuestionsCalories.get(userId).get(QuestionsCalories.AGE));
        String sex = responsesUserOnQuestionsCalories.get(userId).get(QuestionsCalories.SEX);
        String activity = responsesUserOnQuestionsCalories.get(userId).get(QuestionsCalories.ACTIVITY);
        String result = String.format("""
                С учетом введенных вами данных, ваш результат будет равен %d ккал в день для поддержания веса.
                Для сброса веса убавьте 500 ккал, а для набора добавьте 500 ккал.
                """, calorieCalculator(weight, height, age, sex, activity));
        sendMessageWithRemovedKeyboard(result, userId, deleteKeyboard());
        userCommandStates.remove(userId);
        iteratorUserById.remove(userId);
        responsesUserOnQuestionsCalories.remove(userId);
    }

    public void addResponseOnQuestion(String userId, String text, int iterator) {
        if (iterator > 0 && iterator <= questionsCalories.length) {
            Map<QuestionsCalories, String> responseOnQuestion;
            if (responsesUserOnQuestionsCalories.get(userId) == null) {
                responseOnQuestion = new EnumMap<>(QuestionsCalories.class);
                responseOnQuestion.put(questionsCalories[iterator - 1], text);
                responsesUserOnQuestionsCalories.put(userId, responseOnQuestion);
            } else {
                responseOnQuestion = responsesUserOnQuestionsCalories.get(userId);
                responseOnQuestion.put(questionsCalories[iterator - 1], text);
                responsesUserOnQuestionsCalories.replace(userId, responseOnQuestion);
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

    @Override
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

    @Override
    public void sendMessageWithRemovedKeyboard(String text, String userId, ReplyKeyboard replyKeyboard) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .replyMarkup(replyKeyboard)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    @Override
    public void sendKeyboardWithHtmlTextSupport(String text, String userId, ReplyKeyboard replyKeyboard) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .replyMarkup(replyKeyboard)
                    .parseMode("HTML")
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    @Override
    public void sendKeyboardWithoutAnyModSupport(String text, String userId, ReplyKeyboard replyKeyboard) {
        try {
            execute(SendMessage.builder()
                    .chatId(userId)
                    .text(text)
                    .replyMarkup(replyKeyboard)
                    .build());
        } catch (TelegramApiException e) {
            getError(e);
        }
    }

    @Override
    public int increment(int iterator) {
        iterator++;
        return iterator;
    }

    @Override
    public int decrement(int iterator) {
        iterator--;
        return iterator;
    }
}
