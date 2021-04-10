package com.su.Model;

public class Candle {
  float open;
  float high;
  float low;
  float close;

  public Candle(){}

  public Candle(float open, float high, float low, float close) {
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
  }

  public float getOpen() {
    return this.open;
  }

  public void setOpen(float open) {
    this.open = open;
  }

  public float getHigh() {
    return this.high;
  }

  public void setHigh(float high) {
    this.high = high;
  }

  public float getLow() {
    return this.low;
  }

  public void setLow(float low) {
    this.low = low;
  }

  public float getClose() {
    return this.close;
  }

  public void setClose(float close) {
    this.close = close;
  }

  @Override
  public String toString() {
    return "{" +
      " open='" + getOpen() + "'" +
      ", high='" + getHigh() + "'" +
      ", low='" + getLow() + "'" +
      ", close='" + getClose() + "'" +
      "}";
  }

}
