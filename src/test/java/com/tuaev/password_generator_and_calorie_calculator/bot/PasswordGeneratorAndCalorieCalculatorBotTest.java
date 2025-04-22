package com.tuaev.password_generator_and_calorie_calculator.bot;

import com.tuaev.password_generator_and_calorie_calculator.configuration_properties_bot.ConfigurationPropertiesBot;
import com.tuaev.password_generator_and_calorie_calculator.enums.Commands;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.HashMap;
import java.util.Map;

class PasswordGeneratorAndCalorieCalculatorBotTest {

    @Mock
    private Message message;
    @Mock
    private ConfigurationPropertiesBot configurationPropertiesBot;
    @Mock
    private ReplyKeyboardRemove keyboard;
    @InjectMocks
    private PasswordGeneratorAndCalorieCalculatorBot bot;
    private final Map<String, Commands> userCommandStates = new HashMap<>();
    private final Map<String, Integer> iteratorUserById = new HashMap<>();
    private String userId = "1";

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        bot = new PasswordGeneratorAndCalorieCalculatorBot(configurationPropertiesBot);
    }

    @Test
    void checkMessageOnCommand_userInState_returnsTrue(){
        Mockito.when(message.isCommand()).thenReturn(true);
        Mockito.when(message.getText()).thenReturn(Commands.CALORIES.getText());
        userCommandStates.put(userId, Commands.CALORIES);
        boolean result = bot.checkMessageOnCommand(message, userId);
        Assertions.assertTrue(result);
    }
}
