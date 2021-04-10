package com.su.Model;

import java.util.List;

public class PriceAlerts {
  private int chatId;
  private List<Float> priceList;

  public PriceAlerts(int chatId, List<Float> priceList) {
    this.chatId = chatId;
    this.priceList = priceList;
  }

  public int getChatId() {
    return this.chatId;
  }

  public void setChatId(int chatId) {
    this.chatId = chatId;
  }

  public List<Float> getPriceList() {
    return this.priceList;
  }

  public void setPriceList(List<Float> priceList) {
    this.priceList = priceList;
  }

  @Override
  public String toString() {
    return "{" +
      " chatId='" + getChatId() + "'" +
      ", priceList='" + getPriceList() + "'" +
      "}";
  }

}
