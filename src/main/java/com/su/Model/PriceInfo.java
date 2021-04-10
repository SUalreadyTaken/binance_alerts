package com.su.Model;

public class PriceInfo {
  float price;
  String msg;

  public PriceInfo(float price, String msg) {
    this.price = price;
    this.msg = msg;
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
      " price='" + getPrice() + "'" +
      ", msg='" + getMsg() + "'" +
      "}";
  }

}
