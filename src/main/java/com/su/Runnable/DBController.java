package com.su.Runnable;

import java.util.concurrent.BlockingQueue;

import com.su.Model.DbInput;
import com.su.Model.PriceInfo;
import com.su.Service.ChatCoinAlertService;


public class DBController implements Runnable {
  private ChatCoinAlertService chatCoinAlertService;
  private BlockingQueue<DbInput> dbQueue;

  public DBController(ChatCoinAlertService chatCoinAlertService, BlockingQueue<DbInput> dbQueue) {
    this.chatCoinAlertService = chatCoinAlertService;
    this.dbQueue = dbQueue;
  }

  public BlockingQueue<DbInput> getDbQueue() {
    return this.dbQueue;
  }

  @Override
  public void run() {
    while(true) {
      try {
        DbInput dbInput = dbQueue.take();
        switch(dbInput.getDbCommand()) {
          case ADD:
            addToDb(dbInput);
            break;
          case REMOVE_CHAT:
            removeFormDbChat(dbInput);
            break;
          case ADDT:
            addToDbWithText(dbInput);
            break;
          case REMOVE_BINANCE:
            removeFromDbBinance(dbInput);
          default:
            break;
        }
      } catch (Exception e) {
      }
    }
  }

  
  private void addToDb(DbInput dbInput) {
    chatCoinAlertService.addMultiplePricesToChatsAlertList(dbInput.getChatId(), dbInput.getSymbol() ,dbInput.getPriceInfoList());
  }
  
  private void removeFormDbChat(DbInput dbInput) {
    chatCoinAlertService.removeMultiplePricesFromChatsAlertList(dbInput.getChatId(), dbInput.getSymbol(), dbInput.getPriceInfoList());
  }
  
  private void addToDbWithText(DbInput dbInput) {
    PriceInfo pi = dbInput.getPriceInfoList().get(0);
    chatCoinAlertService.addPriceToChatsAlertList(dbInput.getChatId(), dbInput.getSymbol(), pi.getPrice(), pi.getMsg());
  }
  
  private void removeFromDbBinance(DbInput dbInput) {
    chatCoinAlertService.removePricesFormSameSymbol(dbInput.getSymbol(), dbInput.getAlertsHit());
  }
}
