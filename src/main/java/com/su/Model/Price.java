package com.su.Model;

import java.util.HashMap;
import java.util.Map;

import com.su.Enum.CoinSymbol;

import org.springframework.stereotype.Component;

@Component
public class Price {

  private Map<CoinSymbol, Float> priceMap = new HashMap<>();


  public Map<CoinSymbol,Float> getPriceMap() {
    return this.priceMap;
  }

}