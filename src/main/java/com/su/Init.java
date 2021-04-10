package com.su;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.su.Enum.CoinSymbol;
import com.su.Model.AllSleep;
import com.su.Model.ChatCoinAlert;
import com.su.Model.CoinAlert;
import com.su.Model.DBQueue;
import com.su.Model.MessageToSend;
import com.su.Model.Price;
import com.su.Model.PriceInfo;
import com.su.Model.TelegramMessageQueue;
import com.su.Model.Watchlist;
import com.su.Model.WatchlistCoin;
import com.su.Repository.ChatCoinAlertsRepository;
import com.su.Runnable.DBController;
import com.su.Runnable.ExecuteMessages;
import com.su.Runnable.WatchlistController;
import com.su.Service.ChatCoinAlertService;
import com.su.Service.IdleService;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

@Component
public class Init {

  @Value("${switchapp}")
  private boolean switchApp;
  private final ChatCoinAlertsRepository chatCoinAlertsRepository;
  private final Watchlist watchlist;
  private final MessageToSend messageToSend;
  private final Price price;
  private final TelegramMessageQueue telegramMessageQueue;
  private final DBQueue dbQueue;
  private final AlertBot alertBot;
  private final ChatCoinAlertService chatCoinAlertService;
  private final IdleService idleService;
  private final TelegramBotsApi botsApi;
  private final AllSleep allSleep;
  private boolean running = true;
  private BotSession botSession;

  public Init(ChatCoinAlertsRepository chatCoinAlertsRepository, Watchlist watchlist, MessageToSend messageToSend,
      Price price, TelegramMessageQueue telegramMessageQueue, DBQueue dbQueue, AlertBot alertBot,
      ChatCoinAlertService chatCoinAlertService, IdleService idleService, AllSleep allSleep) {
    this.allSleep = allSleep;
    this.idleService = idleService;
    this.chatCoinAlertService = chatCoinAlertService;
    this.alertBot = alertBot;
    this.chatCoinAlertsRepository = chatCoinAlertsRepository;
    this.watchlist = watchlist;
    this.messageToSend = messageToSend;
    this.price = price;
    this.telegramMessageQueue = telegramMessageQueue;
    this.dbQueue = dbQueue;
    this.botsApi = new TelegramBotsApi();
  }

  @PostConstruct
  private void start() {
    List<ChatCoinAlert> allAlerts = chatCoinAlertsRepository.findAll();
    initWatchlist(allAlerts);
    if (!switchApp || !idleService.getAlternativeBoolean()) {
      // register telegram bot
      try {
        this.botSession = botsApi.registerBot(alertBot);
        System.out.println("Register bot success !\nusername >> " + alertBot.getBotUsername());
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }
  }

  private void initWatchlist(List<ChatCoinAlert> allAlerts) {
    Map<CoinSymbol, LinkedList<WatchlistCoin>> tmpWatchlist = new LinkedHashMap<>();
    for (int i = 0; i < CoinSymbol.values().length; i++) {
      tmpWatchlist.put(CoinSymbol.values()[i], new LinkedList<>());
    }
    for (ChatCoinAlert cca : allAlerts) {
      int chatId = cca.getChatId();
      for (CoinAlert ca : cca.getCoinAlertList()) {
        CoinSymbol symbol = ca.getSymbol();
        List<WatchlistCoin> chatsWatchlist = new ArrayList<>();
        for (PriceInfo priceInfo : ca.getPriceInfoList()) {
          chatsWatchlist.add(new WatchlistCoin(chatId, priceInfo.getPrice(), priceInfo.getMsg()));
        }
        tmpWatchlist.get(symbol).addAll(chatsWatchlist);
      }
    }

    watchlist.setPricesWatchlist(tmpWatchlist);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    new Thread(new ExecuteMessages(messageToSend, alertBot, allSleep)).start();
    new Thread(new WatchlistController(messageToSend, price, watchlist, chatCoinAlertService,
        telegramMessageQueue.getMessageQueue(), dbQueue.getDbQueue())).start();
    new Thread(new DBController(chatCoinAlertService, dbQueue.getDbQueue())).start();
  }

  void registerOrStop() {
    if (running) {
      System.out.println("bot is running going to stop it");
      botSession.stop();
    } else {
      System.out.println("bot is stopped will start it again");
      botSession.start();
    }
    running = !running;
  }
}
