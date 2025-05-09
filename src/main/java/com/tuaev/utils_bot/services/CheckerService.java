package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.Commands;

import java.time.LocalDateTime;
import java.util.Map;

public interface CheckerService {

    void checkTimeLastActivityWithBot(Map<String, LocalDateTime> dateLastMessageUser, Map<String, Commands> userCommandStates, Map<String, Integer> iteratorUserById);
}
