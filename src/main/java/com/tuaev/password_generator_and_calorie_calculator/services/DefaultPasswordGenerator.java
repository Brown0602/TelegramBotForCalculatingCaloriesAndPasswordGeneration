package com.tuaev.password_generator_and_calorie_calculator.services;

import com.tuaev.password_generator_and_calorie_calculator.enums.Chars;
import com.tuaev.password_generator_and_calorie_calculator.enums.QuestionsPassword;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Service
public class DefaultPasswordGenerator implements PasswordGeneratorService {

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
    public InlineKeyboardMarkup getInlineKeyboardCharacters() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInlineButtons = getInlineButtonsCharacters();
        inlineKeyboardMarkup.setKeyboard(rowsInlineButtons);
        return inlineKeyboardMarkup;
    }

    @Override
    public List<List<InlineKeyboardButton>> getInlineButtonsCharacters() {
        List<List<InlineKeyboardButton>> rowsInlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineButtons = new ArrayList<>();
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
        inlineButtons.add(numbers);
        inlineButtons.add(specialCharacters);
        inlineButtons.add(lowerCase);
        inlineButtons.add(upperCase);
        rowsInlineButtons.add(inlineButtons);
        return rowsInlineButtons;
    }
}
