package com.su.Model;

import com.su.Enum.CoinSymbol;

public class ChatAlert {
  private CoinSymbol symbol;
  private int chatId;
  private float price;
  private String msg;

  public ChatAlert(CoinSymbol symbol, int chatId, float price, String msg) {
    this.symbol = symbol;
    this.chatId = chatId;
    this.price = price;
    this.msg = msg;
  }

  public String getMsg() {
    return this.msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public CoinSymbol getSymbol() {
    return this.symbol;
  }

  public void setSymbol(CoinSymbol symbol) {
    this.symbol = symbol;
  }

  public int getChatId() {
    return this.chatId;
  }

  public void setChatId(int chatId) {
    this.chatId = chatId;
  }

  public float getPrice() {
    return this.price;
  }

  public void setPrice(float price) {
    this.price = price;
  }
  
}
