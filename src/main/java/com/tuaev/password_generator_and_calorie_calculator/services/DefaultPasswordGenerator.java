package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.Chars;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultPasswordGenerator implements PasswordGeneratorService, SendKeyboardCharactersService {

    private final QuestionsPassword[] questionsPassword = QuestionsPassword.values();
    private final Chars[] chars = Chars.values();
    private final Map<String, Map<QuestionsPassword, List<String>>> responsesUserOnQuestionsPassword = new HashMap<>();


    @Override
    public String getTextQuestionPasswordByIterator(int iterator) {
        return questionsPassword[iterator].getText();
    }

    @Override
    public List<Chars> getChars() {
        return Arrays.stream(chars).toList();
    }

    @Override
    public int getLengthQuestionsPassword() {
        return questionsPassword.length;
    }

    @Override
    public boolean addResponseOnQuestion(String userId, String text, int iterator) {
        if (iterator > 0 && iterator <= questionsPassword.length) {
            List<String> responses;
            Map<QuestionsPassword, List<String>> responseOnQuestion;
            if (responsesUserOnQuestionsPassword.get(userId) == null) {
                responses = new ArrayList<>();
                responseOnQuestion = new EnumMap<>(QuestionsPassword.class);
                responses.add(text);
                responseOnQuestion.put(questionsPassword[iterator - 1], responses);
                responsesUserOnQuestionsPassword.put(userId, responseOnQuestion);
                return true;
            } else {
                responseOnQuestion = responsesUserOnQuestionsPassword.get(userId);
                responses = responseOnQuestion.get(questionsPassword[iterator - 1]);
                boolean isUniqueResponse = checkOnUniqueResponse(text, iterator, responseOnQuestion);
                if (isUniqueResponse) {
                    responses.add(text);
                    responseOnQuestion.put(questionsPassword[iterator - 1], responses);
                    responsesUserOnQuestionsPassword.replace(userId, responseOnQuestion);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean checkOnUniqueResponse(String text, int iterator, Map<QuestionsPassword, List<String>> responseOnQuestion) {
        return responseOnQuestion.get(questionsPassword[iterator - 1])
                .stream()
                .noneMatch(response ->
                response.equals(text));
    }

    @Override
    public QuestionsPassword getQuestionPasswordByIterator(int iterator) {
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
