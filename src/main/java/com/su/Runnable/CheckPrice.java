package com.su.Runnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.su.Enum.CoinSymbol;
import com.su.Enum.DBCommand;
import com.su.Model.Candle;
import com.su.Model.ChatAlert;
import com.su.Model.DBQueue;
import com.su.Model.DbInput;
import com.su.Model.Message;
import com.su.Model.MessageToSend;
import com.su.Model.Price;
import com.su.Model.Watchlist;
import com.su.Model.WatchlistCoin;
import com.su.Service.BinanceDataService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckPrice {
  private final Watchlist watchlist;
  private final BinanceDataService binanceDataService;
  private final MessageToSend messageToSend;
  private final Price price;
  private final DBQueue dbQueue;

  public CheckPrice(Watchlist watchlist, BinanceDataService binanceDataService, MessageToSend messageToSend,
      Price price, DBQueue dbQueue) {
    this.watchlist = watchlist;
    this.binanceDataService = binanceDataService;
    this.messageToSend = messageToSend;
    this.price = price;
    this.dbQueue = dbQueue;
  }

  @Scheduled(fixedDelay = 100)
  public void run() {
    synchronized (watchlist.getPricesWatchlist()) {
      Iterator<Map.Entry<CoinSymbol, LinkedList<WatchlistCoin>>> watchListIterator = watchlist.getPricesWatchlist()
          .entrySet().iterator();
      while (watchListIterator.hasNext()) {
        Map.Entry<CoinSymbol, LinkedList<WatchlistCoin>> entry = watchListIterator.next();
        Candle candle = binanceDataService.getBinanceCandle(entry.getKey());
        List<ChatAlert> removeFromDB = null;
        Long startTime = System.currentTimeMillis();
        if (candle != null) {
          price.getPriceMap().put(entry.getKey(), candle.getClose());
          Iterator<WatchlistCoin> it = entry.getValue().iterator();
          while (it.hasNext()) {
            WatchlistCoin alert = it.next();
            if (isBetween(candle.getLow(), candle.getHigh(), alert.getPrice())) {
              String text;
              if (candle.getOpen() < candle.getClose()) {
                // Price is rising for 2 mins
                text = "ALERT " + entry.getKey().label + " rose above " + alert.getPrice() + " " + alert.getMsg();
              } else {
                // Price is falling
                text = "ALERT " + entry.getKey().label + " fell below " + alert.getPrice() + " " + alert.getMsg();
              }
              try {
                messageToSend.getMessageQueue().put(new Message(alert.getChatId(), text));
              } catch (InterruptedException e) {
                System.out.println("CheckPrice error in put to messageQueue");
              }
              if (removeFromDB == null) {
                removeFromDB = new ArrayList<>();
              }
              removeFromDB.add(new ChatAlert(entry.getKey(), alert.getChatId(), alert.getPrice(), ""));
              try {
                watchlist.getToRemoveQueue()
                    .put(new ChatAlert(entry.getKey(), alert.getChatId(), alert.getPrice(), ""));
              } catch (InterruptedException e) {
                System.err.println("Error in puting to watchLis toremovequeue in CheckPrice");
              }
            }
          }
          if (removeFromDB != null) {
            try {
              dbQueue.getDbQueue()
                  .put(new DbInput(DBCommand.REMOVE_BINANCE, entry.getKey(), convertFromChatAlert(removeFromDB)));
            } catch (Exception e) {
            }
          }
          watchlist.modifyWatchList();
          try {
            TimeUnit.MILLISECONDS.sleep(1000 - (startTime - System.currentTimeMillis()));
          } catch (InterruptedException e) {
            System.out.println("CheckPrice error in trying to sleep");
          }
        }
      }
    }
  }

  /**
   * Check if C is between or equal to A and B
   */
  private boolean isBetween(float a, float b, float c) {
    return b >= a ? c >= a && c <= b : c >= b && c <= a;
  }

  private Map<Integer, List<Float>> convertFromChatAlert(List<ChatAlert> chatAlertList) {
    Map<Integer, List<Float>> result = new HashMap<>();
    for (ChatAlert ca : chatAlertList) {
      if (!result.containsKey(ca.getChatId())) {
        List<Float> priceList = chatAlertList.stream().filter(c -> c.getChatId() == ca.getChatId())
            .map(cc -> cc.getPrice()).collect(Collectors.toList());
        result.put(ca.getChatId(), priceList);
      }
    }
    return result;
  }
}
