package com.tuaev.utils_bot.bot;

import com.tuaev.utils_bot.configuration_properties_bot.ConfigurationPropertiesBot;
import com.tuaev.utils_bot.enums.*;
import com.tuaev.utils_bot.exeception.NotUniqueResponseException;
import com.tuaev.utils_bot.exeception.NotValidCommandException;
import com.tuaev.utils_bot.exeception.NotValidDataException;
import com.tuaev.utils_bot.exeception.NotValidDataPasswordGeneratorException;
import com.tuaev.utils_bot.services.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class UtilsBot extends TelegramLongPollingBot implements SendMessageService, IteratorService {

    private final Logger logger = Logger.getLogger(UtilsBot.class.getName());
    private final ConfigurationPropertiesBot configurationPropertiesBot;
    private final CheckerService checkerService;
    private final CalorieCalculatorService calorieCalculatorService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final Map<String, LocalDateTime> dateLastMessageUser = new ConcurrentHashMap<>();
    private final Map<String, Commands> userCommandStates = new ConcurrentHashMap<>();
    private final Map<String, Integer> iteratorUserById = new ConcurrentHashMap<>();
    private final Commands[] commands = Commands.values();

    public UtilsBot(ConfigurationPropertiesBot configurationPropertiesBot, CheckerService checkerService, CalorieCalculatorService calorieCalculatorService, PasswordGeneratorService passwordGeneratorService) {
        this.configurationPropertiesBot = configurationPropertiesBot;
        this.checkerService = checkerService;
        this.calorieCalculatorService = calorieCalculatorService;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @PostConstruct
    public void scheduleActivityChecks() {
        checkerService.checkTimeLastActivityWithBot(dateLastMessageUser, userCommandStates, iteratorUserById);
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

    public void processingCommand(String text, String userId, Update update) {
        Commands state = userCommandStates.get(userId);
        int iterator = 0;
        if (state != null) {
            iterator = iteratorUserById.get(userId);
        }
        if (state == Commands.CALORIES) {
            try {
                iterator = calorieCalculatorService.checkValidDataOnQuestionsAboutCalories(iterator, userId, text);
            } catch (NotValidDataException e) {
                sendMessage(e.getMessage(), userId);
                iterator = decrement(iterator);
                replaceIteratorByUserId(userId, iterator);
            }
            QuestionsCalories question = calorieCalculatorService.getQuestionAboutCaloriesByIterator(iterator);
            sendQuestionAboutCalories(question, userId, iterator);
            if (calorieCalculatorService.isFullnessResponsesUsersOnQuestionsAboutCalories(userId)) {
                sendMessageWithRemovedKeyboard(calorieCalculatorService.finalizeCalorieCalculation(userId),
                        userId, deleteKeyboard());
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
        }
    }

    public void sendQuestionAboutCalories(QuestionsCalories question, String userId, int iterator) {
            if (question == QuestionsCalories.WEIGHT || question == QuestionsCalories.HEIGHT || question == QuestionsCalories.AGE) {
                sendMessage(question.getText(), userId);
                iterator = increment(iterator);
            } else if (question == QuestionsCalories.SEX) {
                sendKeyboardWithoutAnyModSupport(question.getText(), userId, getReplyKeyboardMarkupSex());
                iterator = increment(iterator);
            } else if (question == QuestionsCalories.ACTIVITY) {
                String text = calorieCalculatorService.getActivities();
                sendMessage(question.getText(), userId);
                sendKeyboardWithHtmlTextSupport(text, userId, getKeyboardActivity());
                iterator = increment(iterator);
            }
            replaceIteratorByUserId(userId, iterator);
    }

    private void replaceIteratorByUserId(String userId, int iterator) {
        iteratorUserById.replace(userId, iterator);
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
