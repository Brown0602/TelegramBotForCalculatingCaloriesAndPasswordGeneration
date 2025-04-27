package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.Chars;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
    public InlineKeyboardMarkup getInlineKeyboardMarkupCharacters() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        InlineKeyboardButton numbers = new InlineKeyboardButton();
        numbers.setText("1\uFE0F⃣");
        numbers.setCallbackData(Chars.NUMBERS.getInfo());
        InlineKeyboardButton specialCharacters = new InlineKeyboardButton();
        specialCharacters.setText("2\uFE0F⃣");
        specialCharacters.setCallbackData(Chars.SPECIAL_CHARACTERS.getInfo());
        InlineKeyboardButton lowerCase = new InlineKeyboardButton();
        lowerCase.setText("3\uFE0F⃣");
        lowerCase.setCallbackData(Chars.LOWER_CASE.getInfo());
        InlineKeyboardButton upperCase = new InlineKeyboardButton();
        upperCase.setText("4\uFE0F⃣");
        upperCase.setCallbackData(Chars.UPPER_CASE.getInfo());
        inlineKeyboardButtons.add(numbers);
        inlineKeyboardButtons.add(specialCharacters);
        inlineKeyboardButtons.add(lowerCase);
        inlineKeyboardButtons.add(upperCase);
        rowsInline.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
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
