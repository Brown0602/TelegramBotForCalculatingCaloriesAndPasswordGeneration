package com.tuaev.utils_bot.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public interface SendMessageService {

    void sendKeyboardWithoutAnyModSupport(String userId, String text, ReplyKeyboard replyKeyboard);
    void sendKeyboardWithHtmlTextSupport(String text, String userId, ReplyKeyboard replyKeyboard);
    void sendMessageWithRemovedKeyboard(String text, String userId, ReplyKeyboard replyKeyboard);
    void sendMessage(String text, String userId);

}
