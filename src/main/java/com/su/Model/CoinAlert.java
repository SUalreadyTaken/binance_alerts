package com.su.Model;

import java.util.List;

import com.su.Enum.CoinSymbol;

public class CoinAlert {
  
  private CoinSymbol symbol;
  private List<PriceInfo> priceInfoList;



  public CoinAlert(CoinSymbol symbol, List<PriceInfo> priceInfoList) {
    this.symbol = symbol;
    this.priceInfoList = priceInfoList;
  }

  public CoinSymbol getSymbol() {
    return this.symbol;
  }

  public void setSymbol(CoinSymbol symbol) {
    this.symbol = symbol;
  }

  public List<PriceInfo> getPriceInfoList() {
    return this.priceInfoList;
  }

  public void setPriceInfoList(List<PriceInfo> priceInfoList) {
    this.priceInfoList = priceInfoList;
  }

  @Override
  public String toString() {
    return "{" +
      " symbol='" + getSymbol() + "'" +
      ", priceList='" + getPriceInfoList() + "'" +
      "}";
  }

}
