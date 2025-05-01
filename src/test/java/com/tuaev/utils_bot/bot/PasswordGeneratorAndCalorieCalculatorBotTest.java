package com.tuaev.utils_bot.bot;

import com.tuaev.utils_bot.configuration_properties_bot.ConfigurationPropertiesBot;
import com.tuaev.utils_bot.enums.Commands;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private UtilsBot bot;
    private final Map<String, Commands> userCommandStates = new HashMap<>();
    private final Map<String, Integer> iteratorUserById = new HashMap<>();
    private String userId = "1";

//    @BeforeEach
//    void setUp(){
//        MockitoAnnotations.openMocks(this);
//        bot = new UtilsBot(configurationPropertiesBot);
//    }

    @Test
    void checkMessageOnCommand_userInState_returnsTrue(){
        //Mockito.when(bot.sendMessage(Mockito.anyString(), Mockito.anyString()));
        Mockito.when(message.isCommand()).thenReturn(true);
        Mockito.when(message.getText()).thenReturn(Commands.CALORIES.getText());
        userCommandStates.put(userId, Commands.CALORIES);
        //boolean result = bot.checkMessageOnCommandFromUser(message, userId);
        //Assertions.assertTrue(result);
    }
}
