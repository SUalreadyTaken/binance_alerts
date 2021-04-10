package com.su.Service;

import java.util.List;

import javax.annotation.PostConstruct;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.su.Enum.CoinSymbol;
import com.su.Model.Candle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BinanceDataService {
  BinanceApiRestClient client;
  @Value("${binance.api.key}")
  String apiKey;
  @Value("${binance.secret}")
  String secret;

  @PostConstruct
  private void init() {
    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(this.apiKey, this.secret);
    this.client = factory.newRestClient();
  }

  public Candle getBinanceCandle(CoinSymbol symbol) {
    try {
      long start = System.currentTimeMillis();
      // + 10 sometimes only 1 candle is returned ??
      long lookbackTime = start - (2 * 60 * 1000) + 10;
      List<Candlestick> candlesticks = client.getCandlestickBars(symbol.label, CandlestickInterval.ONE_MINUTE, 2,
          lookbackTime, start);
      return candlesticks.size() == 2 ? multiCandle(candlesticks) : singleCandle(candlesticks.get(0));

    } catch (Exception e) {
      System.out.println("Error in binanceDataService\n" + e.toString());
      return null;
    }

  }

  private Candle singleCandle(Candlestick c) {
    return new Candle(Float.valueOf(c.getOpen()), Float.valueOf(c.getHigh()), Float.valueOf(c.getLow()),
        Float.valueOf(c.getClose()));
  }

  private Candle multiCandle(List<Candlestick> candlesticks) {
    Candle candle = new Candle();
    candle.setOpen(Float.valueOf(candlesticks.get(0).getOpen()));
    candle.setClose(Float.valueOf(candlesticks.get(1).getClose()));
    candle.setHigh(Float.valueOf(candlesticks.get(0).getHigh()) > Float.valueOf(candlesticks.get(1).getHigh())
        ? Float.valueOf(candlesticks.get(0).getHigh())
        : Float.valueOf(candlesticks.get(1).getHigh()));
    candle.setLow(Float.valueOf(candlesticks.get(0).getLow()) < Float.valueOf(candlesticks.get(1).getLow())
        ? Float.valueOf(candlesticks.get(0).getLow())
        : Float.valueOf(candlesticks.get(1).getLow()));
    return candle;
  }

}
