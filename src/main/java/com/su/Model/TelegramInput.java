package com.su.Model;

import java.util.List;

import com.su.Enum.TelegramCommand;

public class TelegramInput {
  private TelegramCommand telegramCommand;
  private List<String> msgList;
  private int chatId;

  public TelegramInput(TelegramCommand telegramCommand, List<String> msgList, int chatId) {
    this.telegramCommand = telegramCommand;
    this.msgList = msgList;
    this.chatId = chatId;
  }

  public TelegramCommand getTelegramCommand() {
    return this.telegramCommand;
  }

  public void setTelegramCommand(TelegramCommand telegramCommand) {
    this.telegramCommand = telegramCommand;
  }

  public List<String> getMsgList() {
    return this.msgList;
  }

  public void setMsgList(List<String> msgList) {
    this.msgList = msgList;
  }

  public int getChatId() {
    return this.chatId;
  }

  public void setChatId(int chatId) {
    this.chatId = chatId;
  }


  @Override
  public String toString() {
    return "{" +
      " command='" + getTelegramCommand() + "'" +
      ", msgList='" + getMsgList() + "'" +
      ", chatId='" + getChatId() + "' " +
      "}";
  }

}
