package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.CallbackResponse;
import com.tuaev.utils_bot.enums.Chars;
import com.tuaev.utils_bot.enums.QuestionsPassword;
import com.tuaev.utils_bot.exeception.NotUniqueResponseException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Service
public class DefaultPasswordGenerator implements PasswordGeneratorService {

    private final CallbackResponse[] callbackResponses = CallbackResponse.values();
    private final QuestionsPassword[] questionsPassword = QuestionsPassword.values();
    private final Chars[] chars = Chars.values();
    private final Map<String, Map<QuestionsPassword, List<String>>> responsesUserOnQuestionsPassword = new HashMap<>();


    @Override
    public String getTextQuestionPasswordByIterator(int iterator) {
        return questionsPassword[iterator].getText();
    }

    @Override
    public int getLengthListCharacters() {
        return chars.length;
    }

    @Override
    public int getLengthListOnQuestionAboutCharactersByUserId(String userId) {
        if (!responsesUserOnQuestionsPassword.isEmpty()) {
            return responsesUserOnQuestionsPassword.get(userId).get(QuestionsPassword.CHARACTERS).size();
        }
        return 0;
    }

    private boolean isNotCallbackResponse(String text) {
        return Arrays.stream(callbackResponses).noneMatch(response -> response.getText().equals(text));
    }

    @Override
    public boolean isInlineButtonCharacter(String text) {
        return Arrays.stream(chars).anyMatch(character -> character.getInfo().equals(text));
    }

    @Override
    public int getLengthQuestionsPassword() {
        return questionsPassword.length;
    }

    @Override
    public void addResponseOnQuestionAboutPassword(String userId, String text, QuestionsPassword question) {
        List<String> responses;
        Map<QuestionsPassword, List<String>> responsesOnQuestion;
        if (responsesUserOnQuestionsPassword.get(userId) == null) {
            responses = new ArrayList<>();
            responsesOnQuestion = new EnumMap<>(QuestionsPassword.class);
            responses.add(text);
            responsesOnQuestion.put(question, responses);
            responsesUserOnQuestionsPassword.put(userId, responsesOnQuestion);
        } else {
            responsesOnQuestion = responsesUserOnQuestionsPassword.get(userId);
            if (responsesOnQuestion.get(question) == null) {
                responses = new ArrayList<>();
                responses.add(text);
                responsesOnQuestion.put(question, responses);
                return;
            }
            if (!checkOnUniqueResponse(text, question, responsesOnQuestion)) {
                throw new NotUniqueResponseException("Вы уже выбрали этот вариант\uD83E\uDD14");
            }
            responses = responsesOnQuestion.get(question);
            responses.add(text);
            responsesOnQuestion.put(question, responses);
            responsesUserOnQuestionsPassword.replace(userId, responsesOnQuestion);
        }
    }

    private boolean checkOnUniqueResponse(String text, QuestionsPassword question, Map<QuestionsPassword, List<String>> responseOnQuestion) {
        return responseOnQuestion.get(question)
                .stream()
                .noneMatch(response ->
                        response.equals(text));
    }

    @Override
    public QuestionsPassword getQuestionPasswordByIterator(int iterator) {
        return questionsPassword[iterator];
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardNextQuestion() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(getRowInlineButtonForNextQuestion());
        return inlineKeyboard;
    }

    private List<List<InlineKeyboardButton>> getRowInlineButtonForNextQuestion() {
        List<List<InlineKeyboardButton>> rowInlineButton = new ArrayList<>();
        List<InlineKeyboardButton> inlineButtons = new ArrayList<>();
        InlineKeyboardButton nextQuestion = new InlineKeyboardButton();
        nextQuestion.setText(CallbackResponse.NEXT_QUESTION.getText());
        nextQuestion.setCallbackData(CallbackResponse.NEXT_QUESTION.getText());
        inlineButtons.add(nextQuestion);
        rowInlineButton.add(inlineButtons);
        return rowInlineButton;
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardCharacters() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInlineButtons = getInlineButtonsCharacters();
        inlineKeyboardMarkup.setKeyboard(rowsInlineButtons);
        return inlineKeyboardMarkup;
    }

    private List<List<InlineKeyboardButton>> getInlineButtonsCharacters() {
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
