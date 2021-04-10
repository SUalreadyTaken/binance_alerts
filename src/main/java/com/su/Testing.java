// package com.su;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import javax.annotation.PostConstruct;

// import com.su.Enum.CoinSymbol;
// import com.su.Model.ChatAlert;
// import com.su.Model.ChatCoinAlert;
// import com.su.Model.CoinAlert;
// import com.su.Model.PriceInfo;
// import com.su.Model.Watchlist;
// import com.su.Model.WatchlistCoin;
// import com.su.Service.ChatCoinAlertService;

// import org.springframework.stereotype.Component;

// @Component
// public class Testing {
//   private final ChatCoinAlertService chatCoinAlertService;
//   private final Watchlist watchlist;

//   public Testing(ChatCoinAlertService chatCoinAlertService, Watchlist watchlist) {
//     this.chatCoinAlertService = chatCoinAlertService;
//     this.watchlist = watchlist;
//   }

//   @PostConstruct
//   public void test() {
//     testDb();
//     // testCheckPrice();
//     // testWatchList();
//     System.out.println("finished");
//   }

//   private void testWatchList() {
//     WatchlistCoin wEth1 = new WatchlistCoin(1, 1, "eth");
//     WatchlistCoin wEth21 = new WatchlistCoin(2, 1, "eth");
//     WatchlistCoin wEth2 = new WatchlistCoin(1, 2, "eth");
//     WatchlistCoin wEth3 = new WatchlistCoin(1, 3, "eth");
//     LinkedList<WatchlistCoin> wList = new LinkedList<>();
//     wList.add(wEth1);
//     wList.add(wEth21);
//     wList.add(wEth2);
//     wList.add(wEth3);
//     watchlist.getPricesWatchlist().put(CoinSymbol.ETHUSDT, wList);
//     WatchlistCoin wBtc1 = new WatchlistCoin(1, 1, "btc");
//     WatchlistCoin wBtc21 = new WatchlistCoin(2, 1, "btc");
//     WatchlistCoin wBtc2 = new WatchlistCoin(1, 2, "btc");
//     WatchlistCoin wBtc3 = new WatchlistCoin(1, 3, "btc");
//     LinkedList<WatchlistCoin> bList = new LinkedList<>();
//     bList.add(wBtc1);
//     bList.add(wBtc21);
//     bList.add(wBtc2);
//     bList.add(wBtc3);
//     watchlist.getPricesWatchlist().put(CoinSymbol.BTCUSDT, bList);
//     ChatAlert rEth21 = new ChatAlert(CoinSymbol.ETHUSDT, 2, 1, "");
//     ChatAlert rEth3 = new ChatAlert(CoinSymbol.ETHUSDT, 1, 3, "");
//     try {
//       watchlist.getToRemoveQueue().put(rEth21);
//       watchlist.getToRemoveQueue().put(rEth3);
//     } catch (InterruptedException e) {
//       e.printStackTrace();
//     }
//     watchlist.modifyWatchList();
//     watchlist.getPricesWatchlist().forEach((k, v) -> System.out.println(k + " " + v.toString()));
//     /**
//      * BTCUSDT [ { chatId='1', price='1.0', msg='btc'}, { chatId='2', price='1.0',
//      * msg='btc'}, { chatId='1', price='2.0', msg='btc'}, { chatId='1', price='3.0',
//      * msg='btc'}] 
//      * ETHUSDT [{ chatId='1', price='1.0', msg='eth'}, { chatId='1',
//      * price='2.0', msg='eth'}]
//      */

//   }

//   private void testDb() {
//     List<PriceInfo> tmpData1 = new ArrayList<>();
//     List<PriceInfo> tmpData2 = new ArrayList<>();
//     tmpData1.add(new PriceInfo(11f, ""));
//     tmpData1.add(new PriceInfo(12f, ""));
//     tmpData1.add(new PriceInfo(13f, ""));
//     tmpData2.add(new PriceInfo(21f, ""));
//     tmpData2.add(new PriceInfo(22f, ""));
//     tmpData2.add(new PriceInfo(23f, ""));
//     List<CoinAlert> caList1 = new ArrayList<>();
//     List<CoinAlert> caList2 = new ArrayList<>();
//     caList1.add(new CoinAlert(CoinSymbol.BTCUSDT, tmpData1));
//     caList2.add(new CoinAlert(CoinSymbol.BTCUSDT, tmpData2));
//     List<ChatCoinAlert> addList = new ArrayList<>();
//     addList.add(new ChatCoinAlert(1, caList1));
//     addList.add(new ChatCoinAlert(2, caList2));

//     // chatCoinAlertService.addPricesToChatsAlertLists(addList);
//     chatCoinAlertService.removePricesFormChatsAlertLists(addList);

//     // ------------------------

//     // chatCoinAlertService.addPriceToChatsAlertList(1, CoinSymbol.BTCUSDT,
//     // 1022.1231f, null);
//     // chatCoinAlertService.addPriceToChatsAlertList(1, CoinSymbol.BTCUSDT,
//     // 222.1231f, null);
//     // chatCoinAlertService.addPriceToChatsAlertList(1, CoinSymbol.BTCUSDT,
//     // 32.1231f, null);

//     // chatCoinAlertService.removePriceFromChatsAlertList(1, CoinSymbol.BTCUSDT,
//     // 1022.1231f);
//     // chatCoinAlertService.removePriceFromChatsAlertList(1, CoinSymbol.BTCUSDT,
//     // 222.1231f);
//     // chatCoinAlertService.removePriceFromChatsAlertList(1, CoinSymbol.BTCUSDT,
//     // 32.1231f);

//     // ------------------------

//     List<PriceInfo> tmpPrices = new ArrayList<>();
//     tmpPrices.add(new PriceInfo(11, "asd"));
//     tmpPrices.add(new PriceInfo(12, "asd"));
//     tmpPrices.add(new PriceInfo(13, "asd"));

//     chatCoinAlertService.addMultiplePricesToChatsAlertList(1, CoinSymbol.BTCUSDT, tmpPrices);
//     List<PriceInfo> tmpPrices2 = new ArrayList<>();
//     tmpPrices2.add(new PriceInfo(21, "asd"));
//     tmpPrices2.add(new PriceInfo(22, "asd"));
//     tmpPrices2.add(new PriceInfo(23, "asd"));
//     chatCoinAlertService.addMultiplePricesToChatsAlertList(2, CoinSymbol.BTCUSDT, tmpPrices2);
//     // chatCoinAlertService.removeMultiplePricesFromChatsAlertList(1,
//     // CoinSymbol.BTCUSDT, tmpPrices);

//     // -----------------------------

//   }

//   private void testCheckPrice() {
//     List<ChatAlert> chatAlertList = new ArrayList<>();
//     chatAlertList.add(new ChatAlert(CoinSymbol.BTCUSDT, 1, 11, ""));
//     chatAlertList.add(new ChatAlert(CoinSymbol.BTCUSDT, 2, 21, ""));
//     chatAlertList.add(new ChatAlert(CoinSymbol.BTCUSDT, 1, 12, ""));
//     chatAlertList.add(new ChatAlert(CoinSymbol.BTCUSDT, 2, 22, ""));
//     chatAlertList.add(new ChatAlert(CoinSymbol.BTCUSDT, 1, 13, ""));
//     // chatAlertList.add(new ChatAlert(2, 23));
//     Map<Integer, List<Float>> toSend = convertFromChatAlert(chatAlertList);
//     // for (Entry<Integer, List<Float>> entry : toSend.entrySet()) {
//     // System.out.println("id:" + entry.getKey() + " " + entry.getValue());
//     // }
//     chatCoinAlertService.removePricesFormSameSymbol(CoinSymbol.BTCUSDT, toSend);
//   }

//   private Map<Integer, List<Float>> convertFromChatAlert(List<ChatAlert> chatAlertList) {
//     Map<Integer, List<Float>> result = new HashMap<>();
//     for (ChatAlert ca : chatAlertList) {
//       if (!result.containsKey(ca.getChatId())) {
//         List<Float> priceList = chatAlertList.stream().filter(c -> c.getChatId() == ca.getChatId())
//             .map(cc -> cc.getPrice()).collect(Collectors.toList());
//         result.put(ca.getChatId(), priceList);
//       }
//     }
//     return result;
//   }
// }
