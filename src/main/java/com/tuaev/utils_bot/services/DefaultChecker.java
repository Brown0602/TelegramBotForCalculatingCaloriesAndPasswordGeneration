package com.tuaev.utils_bot.services;

import com.tuaev.utils_bot.enums.Commands;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class DefaultChecker implements CheckerService {

    private final Logger logger = Logger.getLogger(DefaultChecker.class.getName());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Override
    public void checkTimeLastActivityWithBot(Map<String, LocalDateTime> dateLastMessageUser, Map<String, Commands> userCommandStates, Map<String, Integer> iteratorUserById) {
        final Runnable checkUser = () -> {
            for (Map.Entry<String, LocalDateTime> entry : dateLastMessageUser.entrySet()) {
                String userId = entry.getKey();
                String msg;
                LocalDateTime timeLastInteractions = entry.getValue();
                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(timeLastInteractions, now);
                long seconds = duration.toSeconds();
                msg = String.format("Пользователь с id: %s был активен %d секунд назад", userId, seconds);
                logger.log(Level.INFO, msg);
                if (seconds > 180) {
                    dateLastMessageUser.remove(userId);
                    userCommandStates.remove(userId);
                    iteratorUserById.remove(userId);
                    msg = String.format("Пользователь с id %s удален из кэша\nОн был неактивен более %d секунд", userId, 180);
                    logger.log(Level.INFO, msg);
                    return;
                }
                return;
            }
            logger.log(Level.INFO, () -> "Нет активных пользователей");
        };
        scheduledExecutorService.scheduleWithFixedDelay(checkUser, 60, 60, TimeUnit.SECONDS);
    }
}
