package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.Chars;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Service
public class DefaultPasswordGenerator implements PasswordGeneratorService, SendKeyboardCharactersService{

    private final QuestionsPassword[] questionsPassword = QuestionsPassword.values();
    private final Map<String, List<Map<QuestionsPassword, List<String>>>> responsesUserOnQuestionsPassword = new HashMap<>();


    @Override
    public String getTextQuestionPasswordByIterator(int iterator){
        return questionsPassword[iterator].getText();
    }

    @Override
    public int getLengthQuestionsPassword(){
        return questionsPassword.length;
    }

    @Override
    public void addResponseOnQuestion(String userId, String text, int iterator){
        if (iterator > 0 && iterator <= questionsPassword.length) {
            List<String> responses = new ArrayList<>();
            responses.add(text);
            Map<QuestionsPassword, List<String>> responseOnQuestion = new EnumMap<>(QuestionsPassword.class);
            responseOnQuestion.put(questionsPassword[iterator - 1], responses);
            List<Map<QuestionsPassword, List<String>>> list;
            if (responsesUserOnQuestionsPassword.get(userId) == null) {
                list = new ArrayList<>();
                list.add(responseOnQuestion);
                responsesUserOnQuestionsPassword.put(userId, list);
            } else {
                list = responsesUserOnQuestionsPassword.get(userId);
                list.add(responseOnQuestion);
                responsesUserOnQuestionsPassword.replace(userId, list);
            }
        }
    }

    @Override
    public QuestionsPassword getQuestionPasswordByIterator(int iterator){
        return questionsPassword[iterator];
    }

    @Override
    public ReplyKeyboardMarkup getReplyKeyboardMarkupCharacters() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow keyboardRow = getKeyboardButtonsCharacters();
        rows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    @Override
    public KeyboardRow getKeyboardButtonsCharacters() {
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
}
