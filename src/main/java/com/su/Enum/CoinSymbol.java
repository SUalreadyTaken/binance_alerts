package com.su.Enum;

public enum CoinSymbol {
  BTCUSDT("BTCUSDT"),
  ETHUSDT("ETHUSDT"),
  LTCUSDT("LTCUSDT"),
  LINKUSDT("LINKUSDT"),
  EOSUSDT("EOSUSDT");
  
  public final String label;

  private CoinSymbol(String label) {
    this.label = label;
  }
}
