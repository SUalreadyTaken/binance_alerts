package com.su;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.su.Enum.TelegramCommand;
import com.su.Model.TelegramInput;
import com.su.Model.TelegramMessageQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AlertBot extends TelegramLongPollingBot {

  @Value("${telegram.token}")
  private String token;

  @Value("${telegram.username}")
  private String username;

  private final TelegramMessageQueue telegramMessageQueue;

  public AlertBot(TelegramMessageQueue telegramMessageQueue) {
    this.telegramMessageQueue = telegramMessageQueue;
  }

  @Override
  public void onUpdateReceived(Update update) {
    try {
      String command = update.getMessage().getText();
      int chatId = Math.toIntExact(update.getMessage().getChatId());
      String[] commandStringArray = trimCommand(command);
      String commandString = commandStringArray[0];
  
      switch (commandString.toLowerCase()) {
      case "/add":
        putToQueue(TelegramCommand.ADD, chatId, commandStringArray);
        break;
      case "/remove":
        putToQueue(TelegramCommand.REMOVE, chatId, commandStringArray);
        break;
      case "/addt":
        putToQueue(TelegramCommand.ADDT, chatId, commandStringArray);
        break;
      case "/price":
        putToQueue(TelegramCommand.PRICE, chatId, commandStringArray);
        break;
      case "/edit":
        putToQueue(TelegramCommand.EDIT, chatId, commandStringArray);
        break;
      case "/list":
        putToQueue(TelegramCommand.STATUS, chatId, commandStringArray);
        break;
      default:
        break;
      }
    } catch (Exception e) {
      System.out.println("Error in onUpdateReceived");
      System.out.println(e);
    }

  }

  private String[] trimCommand(String command) {
    // replace multiple spaces with 1, split by spaces
    return command.trim().replaceAll("\\s+", " ").split(" ");
  }

  private void putToQueue(TelegramCommand command, int chatId, String[] message) {
    try {
      telegramMessageQueue.getMessageQueue()
          .put(new TelegramInput(command, Stream.of(message).skip(1).collect(Collectors.toList()), chatId));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }

}
