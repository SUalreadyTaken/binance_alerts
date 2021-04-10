package com.su.Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.su.Enum.CoinSymbol;

import org.springframework.stereotype.Component;

@Component
public class Watchlist {

  private Map<CoinSymbol, LinkedList<WatchlistCoin>> pricesWatchlist = new LinkedHashMap<>();
  private BlockingQueue<ChatAlert> toAddQueue = new LinkedBlockingQueue<>();
  private BlockingQueue<ChatAlert> toRemoveQueue = new LinkedBlockingQueue<>();
  ReadWriteLock lock = new ReentrantReadWriteLock();

  public ReadWriteLock getLock() {
    return this.lock;
  }

  public BlockingQueue<ChatAlert> getToAddQueue() {
    return this.toAddQueue;
  }

  public BlockingQueue<ChatAlert> getToRemoveQueue() {
    return this.toRemoveQueue;
  }

  public Map<CoinSymbol, LinkedList<WatchlistCoin>> getPricesWatchlist() {
    return this.pricesWatchlist;
  }

  public void setPricesWatchlist(Map<CoinSymbol, LinkedList<WatchlistCoin>> pricesWatchlist) {
    this.pricesWatchlist = pricesWatchlist;
  }

  public void modifyWatchList() {
    List<ChatAlert> toRemoveList = new ArrayList<>();
    this.toRemoveQueue.drainTo(toRemoveList);
    List<ChatAlert> toAddList = new ArrayList<>();
    this.toAddQueue.drainTo(toAddList);
    try {
      this.lock.writeLock().lock();
      for (ChatAlert ca : toRemoveList) {
        if (this.pricesWatchlist.containsKey(ca.getSymbol()) && this.pricesWatchlist.get(ca.getSymbol()) != null) {
          removeFromList(this.pricesWatchlist.get(ca.getSymbol()), ca);
        }
      }
      for (ChatAlert ca : toAddList) {
        if (this.pricesWatchlist.containsKey(ca.getSymbol()) && this.pricesWatchlist.get(ca.getSymbol()) != null) {
          this.pricesWatchlist.get(ca.getSymbol()).add(new WatchlistCoin(ca.getChatId(), ca.getPrice(), ca.getMsg()));
        } else {
          this.pricesWatchlist.put(ca.getSymbol(), (LinkedList<WatchlistCoin>) Stream
              .of(new WatchlistCoin(ca.getChatId(), ca.getPrice(), ca.getMsg())).collect(Collectors.toList()));
        }
      }
    } catch (Exception e) {
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  private void removeFromList(LinkedList<WatchlistCoin> list, ChatAlert coin) {
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getChatId() == coin.getChatId() && list.get(i).getPrice() == coin.getPrice()) {
        list.remove(i);
        break;
      }
    }
  }

}
