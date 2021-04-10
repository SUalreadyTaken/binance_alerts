package com.su.Runnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;

import com.su.Enum.CoinSymbol;
import com.su.Enum.DBCommand;
import com.su.Model.ChatAlert;
import com.su.Model.ChatCoinAlert;
import com.su.Model.CoinAlert;
import com.su.Model.DbInput;
import com.su.Model.Message;
import com.su.Model.MessageToSend;
import com.su.Model.Price;
import com.su.Model.PriceInfo;
import com.su.Model.TelegramInput;
import com.su.Model.Watchlist;
import com.su.Model.WatchlistCoin;
import com.su.Service.ChatCoinAlertService;

public class WatchlistController implements Runnable {
  private BlockingQueue<TelegramInput> messageQueue;
  private final MessageToSend messageToSend;
  private final Price priceComponent;
  private final Watchlist watchlist;
  private final BlockingQueue<DbInput> dbQueue;
  private final ChatCoinAlertService chatCoinAlertService;

  public BlockingQueue<TelegramInput> getMessageQueue() {
    return this.messageQueue;
  }

  public WatchlistController(MessageToSend messageToSend, Price price, Watchlist watchlist,
      ChatCoinAlertService chatCoinAlertService, BlockingQueue<TelegramInput> messageQueue,
      BlockingQueue<DbInput> dbQueue) {
    this.dbQueue = dbQueue;
    this.messageToSend = messageToSend;
    this.priceComponent = price;
    this.watchlist = watchlist;
    this.chatCoinAlertService = chatCoinAlertService;
    this.messageQueue = messageQueue;
  }

  @Override
  public void run() {
    while (true) {
      try {
        TelegramInput message = messageQueue.take();
        System.out.println("Got this msg > " + message.toString());
        switch (message.getTelegramCommand()) {
        case ADD:
          addToWatchList(message);
          break;
        case REMOVE:
          removeFromWatchList(message);
          break;
        case EDIT:
          editWatchList(message);
          break;
        case ADDT:
          addToWatchListWithText(message);
          break;
        case STATUS:
          getWatchList(message);
          break;
        case PRICE:
          getPrice(message);
          break;
        default:
          break;
        }
      } catch (Exception e) {
      }
    }
  }

  private void getPrice(TelegramInput message) {
    try {
      if (message.getMsgList().size() > 0) {
        CoinSymbol symbol = findSymbol(message.getMsgList().get(0));
        Float cPrice = priceComponent.getPriceMap().get(symbol);
        StringBuilder respondMessage = new StringBuilder();
        respondMessage.append(symbol.toString()).append(" price is ").append(cPrice);
        messageToSend.getMessageQueue().put(new Message(message.getChatId(), respondMessage.toString()));
      }
    } catch (Exception e) {
      System.out.println("Error in getPrice watchListController");
    }
  }

  private void getWatchList(TelegramInput message) {
    try {
      watchlist.getLock().readLock().lock();
      StringBuilder respondMessage = new StringBuilder();
      CoinSymbol symbol = findSymbol(message.getMsgList().get(0));
      if (symbol != null) {
        LinkedList<WatchlistCoin> coinList = watchlist.getPricesWatchlist().get(symbol);
        if (coinList != null) {
          respondMessage.append(symbol.toString()).append("\n");
          for (WatchlistCoin wc : coinList) {
            respondMessage.append(wc.telegramMessage()).append("\n");
          }
        }
      } else if (message.getMsgList().get(0).equalsIgnoreCase("all")) {
        ChatCoinAlert cca = chatCoinAlertService.getFirstByChatId(message.getChatId());
        if (cca != null) {
          List<CoinAlert> coinAlertList = cca.getCoinAlertList();
          for (CoinAlert ca : coinAlertList) {
            respondMessage.append(ca.getSymbol().toString()).append("\n");
            for (PriceInfo pi : ca.getPriceInfoList()) {
              respondMessage.append(pi.telegramMessage()).append("\n");
            }
            respondMessage.append("\n");
          }
        }
      }
      if (!respondMessage.toString().isEmpty()) {
        try {
          messageToSend.getMessageQueue().put(new Message(message.getChatId(), respondMessage.toString()));
        } catch (Exception e) {
        }
      }
    } catch (Exception e) {
    } finally {
      watchlist.getLock().readLock().unlock();
    }
  }

  /**
   * Adds only 1 price
   */
  private void addToWatchListWithText(TelegramInput message) {
    try {
      watchlist.getLock().readLock().lock();
      StringBuilder respondMessage = new StringBuilder();
      CoinSymbol symbol = findSymbol(message.getMsgList().get(0));
      if (symbol != null) {
        LinkedList<WatchlistCoin> coinList = watchlist.getPricesWatchlist().get(symbol);
        Float currentPrice = priceComponent.getPriceMap().get(symbol);
        if (message.getMsgList().size() > 1) {
          String wantedPriceString = message.getMsgList().get(1);
          if (isNumber(wantedPriceString, respondMessage)) {
            Float wantedPrice = Float.valueOf(wantedPriceString);
            if (isAboveZero(wantedPrice, respondMessage) && isBetweenRange(wantedPrice, respondMessage, currentPrice)) {
              if (coinList != null) {
                boolean found = false;
                for (int i = 0; i < coinList.size(); i++) {
                  if (coinList.get(i).getChatId() == message.getChatId() && coinList.get(i).getPrice() == wantedPrice) {
                    found = true;
                    break;
                  }
                }
                if (!found) {
                  StringBuilder pMessage = new StringBuilder();
                  for (int i = 2; i < message.getMsgList().size(); i++) {
                    pMessage.append(message.getMsgList().get(i)).append(" ");
                  }
                  watchlist.getToAddQueue().put(new ChatAlert(symbol, message.getChatId(), wantedPrice,
                      pMessage.toString().isEmpty() ? "" : pMessage.toString()));
                  respondMessage.append("Added ").append(symbol).append(" ").append(wantedPrice).append(" ")
                      .append(pMessage.toString()).append("to watchlist \n");
                  try {
                    dbQueue.put(new DbInput(DBCommand.ADDT,
                        convertToPriceInfoListWithText(wantedPrice, pMessage.toString()), symbol, message.getChatId()));
                  } catch (Exception e) {
                  }
                }
              }
            }
          }
        }
        if (!respondMessage.toString().isEmpty()) {
          messageToSend.getMessageQueue().put(new Message(message.getChatId(), respondMessage.toString()));
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      watchlist.getLock().readLock().unlock();
    }
  }

  private void editWatchList(TelegramInput message) {
    try {
      watchlist.getLock().readLock().lock();
      StringBuilder respondMessage = new StringBuilder();
      CoinSymbol symbol = findSymbol(message.getMsgList().get(0));
      if (symbol != null) {
        LinkedList<WatchlistCoin> coinList = watchlist.getPricesWatchlist().get(symbol);
        Float currentPrice = priceComponent.getPriceMap().get(symbol);
        if (message.getMsgList().size() == 3) {
          List<Float> pricesForEdit = message.getMsgList().stream()
              .skip(1)
              .filter(s -> isNumber(s, respondMessage))
              .map(Float::valueOf)
              .filter(f -> isAboveZero(f, respondMessage))
              .filter(f -> isBetweenRange(f, respondMessage, currentPrice))
              .collect(Collectors.toList());
          if (coinList != null) {
            for (int i = 0; i < coinList.size(); i++) {
              if (coinList.get(i).getChatId() == message.getChatId()
                  && coinList.get(i).getPrice() == pricesForEdit.get(0)) {
                watchlist.getToRemoveQueue().put(new ChatAlert(symbol, message.getChatId(), pricesForEdit.get(0), ""));
                respondMessage.append("Removed ").append(symbol).append(" ").append(pricesForEdit.get(0))
                    .append(" from watchlist\n");
                try {
                  dbQueue.put(new DbInput(DBCommand.REMOVE_CHAT,
                      convertToPriceInfoList(Stream.of(pricesForEdit.get(0)).collect(Collectors.toList())), symbol,
                      message.getChatId()));
                } catch (Exception e) {
                  System.out.println("Error in editWatchList put dbQueue");
                }
                break;
              }
            }
            watchlist.getToAddQueue().put(new ChatAlert(symbol, message.getChatId(), pricesForEdit.get(1), ""));
            respondMessage.append("Added ").append(symbol).append(" ").append(pricesForEdit.get(1))
                .append(" to watchlist\n");
            try {
              dbQueue.put(new DbInput(DBCommand.ADD,
                  convertToPriceInfoList(Stream.of(pricesForEdit.get(1)).collect(Collectors.toList())), symbol,
                  message.getChatId()));
            } catch (Exception e) {
              System.out.println("Error in editWatchList put dbQueue");
            }
          }
        }
        if (!respondMessage.toString().isEmpty()) {
          messageToSend.getMessageQueue().put(new Message(message.getChatId(), respondMessage.toString()));
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      watchlist.getLock().readLock().unlock();
    }
  }

  private void addToWatchList(TelegramInput message) {
    try {
      watchlist.getLock().readLock().lock();
      StringBuilder respondMessage = new StringBuilder();
      CoinSymbol symbol = findSymbol(message.getMsgList().get(0));
      if (symbol != null) {
        LinkedList<WatchlistCoin> coinList = watchlist.getPricesWatchlist().get(symbol);
        Float currentPrice = priceComponent.getPriceMap().get(symbol);
        if (message.getMsgList().size() > 1) {
          List<Float> pricesToAdd = new ArrayList<>();
          message.getMsgList().stream()
              .skip(1)
              .filter(s -> isNumber(s, respondMessage))
              .map(Float::valueOf)
              .filter(f -> isAboveZero(f, respondMessage))
              .filter(f -> isBetweenRange(f, respondMessage, currentPrice))
              .forEach(f -> {
                if (coinList == null) {
                  try {
                    watchlist.getToAddQueue().put(new ChatAlert(symbol, message.getChatId(), f, ""));
                    respondMessage.append("Added ").append(symbol).append(" ").append(f).append(" to watchlist\n");
                    pricesToAdd.add(f);
                  } catch (Exception e) {
                    System.err.println("Error in add to watchlist queue in addToWatchList tCommander");
                  }
                } else {
                  boolean found = false;
                  for (int i = 0; i < coinList.size(); i++) {
                    if (coinList.get(i).getChatId() == message.getChatId() && coinList.get(i).getPrice() == f) {
                      found = true;
                      break;
                    }
                  }
                  if (!found) {
                    try {
                      watchlist.getToAddQueue().put(new ChatAlert(symbol, message.getChatId(), f, ""));
                      respondMessage.append("Added ").append(symbol).append(" ").append(f).append(" to watchlist\n");
                      pricesToAdd.add(f);
                    } catch (Exception e) {
                      System.err.println("Error in add to watchlist queue in addToWatchList tCommander");
                    }
                  }
                }
              });
          if (!pricesToAdd.isEmpty()) {
            try {
              dbQueue.put(new DbInput(DBCommand.ADD, convertToPriceInfoList(pricesToAdd), symbol, message.getChatId()));
            } catch (Exception e) {
              System.out.println("Error in put dbQueue in addTOWatchList tCommander");
            }
          }
        }
        if (!respondMessage.toString().isEmpty()) {
          messageToSend.getMessageQueue().put(new Message(message.getChatId(), respondMessage.toString()));
        }
      }
    } catch (Exception e) {
      System.err.println("Error in addTOWatchList tCommander");
    } finally {
      watchlist.getLock().readLock().unlock();
    }
  }

  private void removeFromWatchList(TelegramInput message) {
    try {
      watchlist.getLock().readLock().lock();
      StringBuilder respondMessage = new StringBuilder();
      CoinSymbol symbol = findSymbol(message.getMsgList().get(0));
      if (symbol != null) {
        LinkedList<WatchlistCoin> coinList = watchlist.getPricesWatchlist().get(symbol);
        Float currentPrice = priceComponent.getPriceMap().get(symbol);
        if (message.getMsgList().size() > 1) {
          List<Float> pricesToRemove = new ArrayList<>();
          message.getMsgList().stream()
              .skip(1)
              .filter(s -> isNumber(s, respondMessage))
              .map(Float::valueOf)
              .filter(f -> isAboveZero(f, respondMessage))
              .filter(f -> isBetweenRange(f, respondMessage, currentPrice))
              .forEach(f -> {
                if (coinList != null) {
                  boolean found = false;
                  for (int i = 0; i < coinList.size(); i++) {
                    if (coinList.get(i).getChatId() == message.getChatId() && coinList.get(i).getPrice() == f) {
                      found = true;
                      break;
                    }
                  }
                  if (found) {
                    try {
                      watchlist.getToRemoveQueue().put(new ChatAlert(symbol, message.getChatId(), f, ""));
                      respondMessage.append("Removed ").append(symbol).append(" ").append(f)
                          .append(" from watchlist\n");
                      pricesToRemove.add(f);
                    } catch (Exception e) {
                      System.err.println("Error in adding watchlist queue in removeFromWatchList tCommander");
                    }
                  }
                }
              });
          if (!pricesToRemove.isEmpty()) {
            try {
              dbQueue.put(new DbInput(DBCommand.REMOVE_CHAT, convertToPriceInfoList(pricesToRemove), symbol,
                  message.getChatId()));
            } catch (Exception e) {
              System.out.println("Error in put dbQueue in addTOWatchList tCommander");
            }
          }
        }
        if (!respondMessage.toString().isEmpty()) {
          messageToSend.getMessageQueue().put(new Message(message.getChatId(), respondMessage.toString()));
        }
      }
    } catch (Exception e) {
      System.err.println(e);
    } finally {
      watchlist.getLock().readLock().unlock();
    }
  }

  private CoinSymbol findSymbol(String s) {
    CoinSymbol res = null;
    for (int i = 0; i < CoinSymbol.values().length; i++) {
      if (CoinSymbol.values()[i].toString().contains(s.toUpperCase())) {
        res = CoinSymbol.values()[i];
        break;
      }
    }
    return res;
  }

  private boolean isNumber(String s, StringBuilder respondMessage) {
    if (NumberUtils.isParsable(s))
      return true;
    respondMessage.append("[").append(s).append("]").append(" not a number (must use . instead of , )\n");
    return false;

  }

  private boolean isAboveZero(double d, StringBuilder respondMessage) {
    if (d > 0)
      return true;
    respondMessage.append(d).append("can't be negative nubmer \n");
    return false;
  }

  private boolean isBetweenRange(float f, StringBuilder respondMessage, Float price) {
    if (price == null || f > 0 && price * 3 >= f)
      return true;
    respondMessage.append("[").append(f).append("]").append(" out of range 0 - ").append(price * 3).append("\n");
    return false;
  }

  private List<PriceInfo> convertToPriceInfoList(List<Float> prices) {
    List<PriceInfo> res = new ArrayList<>();
    for (Float f : prices) {
      res.add(new PriceInfo(f, ""));
    }
    return res;
  }

  private List<PriceInfo> convertToPriceInfoListWithText(float price, String text) {
    return Stream.of(new PriceInfo(price, text)).collect(Collectors.toList());
  }

}
