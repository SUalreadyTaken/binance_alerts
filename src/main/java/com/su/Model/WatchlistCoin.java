package com.su.Model;

public class WatchlistCoin {
  
  int chatId;
  float price;
  String msg;

  public WatchlistCoin(int chatId, float price, String msg) {
    this.chatId = chatId;
    this.price = price;
    this.msg = msg;
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

  public String getMsg() {
    return this.msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String telegramMessage() {
    return getPrice() + " " + getMsg();
  }

  @Override
  public String toString() {
    return "{" +
      " chatId='" + getChatId() + "'" +
      ", price='" + getPrice() + "'" +
      ", msg='" + getMsg() + "'" +
      "}";
  }
  

}
