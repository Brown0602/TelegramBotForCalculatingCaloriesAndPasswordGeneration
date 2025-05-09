package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.CallbackResponse;
import com.tuaev.utils_bot.enums.Chars;
import com.tuaev.utils_bot.enums.QuestionsPassword;
import com.tuaev.utils_bot.exeception.NotUniqueResponseException;
import com.tuaev.utils_bot.exeception.NotValidDataPasswordGeneratorException;
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
    public boolean isInlineButtonCharacter(String text) {
        return Arrays.stream(chars).anyMatch(character -> character.getInfo().equals(text));
    }

    @Override
    public int getLengthQuestionsPassword() {
        return questionsPassword.length;
    }

    @Override
    public InlineKeyboardMarkup getKeyboardGenerationPassword(){
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(getButtonGenerationPassword());
        return keyboard;
    }

    private List<List<InlineKeyboardButton>> getButtonGenerationPassword(){
        List<List<InlineKeyboardButton>> rowsButtons = new ArrayList<>();
        List<InlineKeyboardButton> rowButtonGenerationPassword = new ArrayList<>();
        InlineKeyboardButton buttonGenerationPassword = new InlineKeyboardButton();
        buttonGenerationPassword.setText(CallbackResponse.GENERATION_PASSWORD.getText());
        buttonGenerationPassword.setCallbackData(CallbackResponse.GENERATION_PASSWORD.getText());
        rowButtonGenerationPassword.add(buttonGenerationPassword);
        rowsButtons.add(rowButtonGenerationPassword);
        return rowsButtons;
    }

    @Override
    public boolean isCallbackResponse(String text){
        return Arrays.stream(callbackResponses).anyMatch(callbackResponse ->
                callbackResponse.getText().equals(text));
    }

    @Override
    public String generationPassword(String userId) {
        Map<QuestionsPassword, List<String>> responsesOnQuestions = responsesUserOnQuestionsPassword.get(userId);
        List<String> desiredCharacters = responsesOnQuestions.get(QuestionsPassword.CHARACTERS);
        int lengthPassword = Integer.parseInt(responsesOnQuestions.get(QuestionsPassword.LENGTH).get(0));
        Random randomDesiredCharacters = new Random();
        Random randomCharacter = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < lengthPassword; i++){
            int r = randomDesiredCharacters.nextInt(0, desiredCharacters.size());
            String infoAboutCharacter = desiredCharacters.get(r);
            Optional<Chars> optional = Arrays.stream(chars).filter(c ->
                    c.getInfo().equals(infoAboutCharacter)).findFirst();
            if (optional.isPresent()){
                char character = optional.get().getValue().charAt(randomCharacter.nextInt(0, optional.get().getValue().length() - 1));
                password.append(character);
            }
        }
        return String.valueOf(password);
    }

    @Override
    public void deleteResponsesUserAboutQuestionsAboutPassword(String userId){
        responsesUserOnQuestionsPassword.remove(userId);
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
            if (question == QuestionsPassword.LENGTH) {
                checkOnIntegerNumbers(text);
                int lengthPassword = Integer.parseInt(text);
                checkOnLengthPassword(lengthPassword);
            }
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

    private void checkOnLengthPassword(int lengthPassword) {
        if (lengthPassword < 8 || lengthPassword > 16){
            throw new NotValidDataPasswordGeneratorException("Неверные данные");
        }
    }

    private void checkOnIntegerNumbers(String text) {
        if (!text.matches("\\d+")) {
            throw new NotValidDataPasswordGeneratorException("Неверные данные");
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
