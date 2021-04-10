package com.su.Model;

import java.util.List;
import java.util.Map;

import com.su.Enum.CoinSymbol;
import com.su.Enum.DBCommand;

public class DbInput {
  private DBCommand dbCommand;
  private CoinSymbol symbol;
  private List<PriceInfo> priceInfoList;
  private Map<Integer, List<Float>> alertsHit;
  private int chatId;


  public DbInput(DBCommand dbCommand, CoinSymbol symbol, Map<Integer, List<Float>> alertsHit) {
    this.dbCommand = dbCommand;
    this.symbol = symbol;
    this.alertsHit = alertsHit;
  }
  
  public DbInput(DBCommand dbCommand, List<PriceInfo> priceInfoList, CoinSymbol symbol, int chatId) {
    this.dbCommand = dbCommand;
    this.symbol = symbol;
    this.priceInfoList = priceInfoList;
    this.chatId = chatId;
  }

  public int getChatId() {
    return this.chatId;
  }

  public List<PriceInfo> getPriceInfoList() {
    return this.priceInfoList;
  }

  public DBCommand getDbCommand() {
    return this.dbCommand;
  }

  public CoinSymbol getSymbol() {
    return this.symbol;
  }

  public Map<Integer,List<Float>> getAlertsHit() {
    return this.alertsHit;
  }
  
}
