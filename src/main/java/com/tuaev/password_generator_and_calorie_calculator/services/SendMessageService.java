package com.tuaev.password_generator_and_calorie_calculator.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

public interface SendMessageService {

    void sendKeyboardWithoutAnyModSupport(String userId, String text, ReplyKeyboardMarkup replyKeyboardMarkup);
    void sendKeyboardWithHtmlTextSupport(String text, String userId, ReplyKeyboardMarkup keyboard);
    void sendMessageWithRemovedKeyboard(String text, String userId, ReplyKeyboardRemove keyboard);
    void sendMessage(String text, String userId);

}
