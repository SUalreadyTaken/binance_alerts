package com.su.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.su.Enum.CoinSymbol;
import com.su.Model.ChatCoinAlert;
import com.su.Model.CoinAlert;
import com.su.Model.PriceInfo;
import com.su.Repository.ChatCoinAlertsRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatCoinAlertService {
  private final ChatCoinAlertsRepository chatCoinAlertsRepository;

  public ChatCoinAlertService(ChatCoinAlertsRepository chatCoinAlertsRepository) {
    this.chatCoinAlertsRepository = chatCoinAlertsRepository;
  }

  private boolean isInWantList(List<PriceInfo> wantedList, double price) {
    for (PriceInfo pi : wantedList) {
      if (pi.getPrice() == price) {
        return true;
      }
    }
    return false;
  }

  public void removeMultiplePricesFromChatsAlertList(int chatId, CoinSymbol symbol, List<PriceInfo> removePriceList) {
    Optional<ChatCoinAlert> inCollection = chatCoinAlertsRepository.findFirstByChatId(chatId);
    if (inCollection.isPresent()) {
      for (int i = 0; i < inCollection.get().getCoinAlertList().size(); i++) {
        if (inCollection.get().getCoinAlertList().get(i).getSymbol().equals(symbol)) {
          List<PriceInfo> priceListInCollection = inCollection.get().getCoinAlertList().get(i).getPriceInfoList();
          if (priceListInCollection.removeIf(c -> isInWantList(removePriceList, c.getPrice()))) {
            if (priceListInCollection.isEmpty()) {
              // only 1 real iteration so can use remove
              inCollection.get().getCoinAlertList().remove(i);
              if (inCollection.get().getCoinAlertList().isEmpty()) {
                chatCoinAlertsRepository.deleteById(inCollection.get().getId());
              } else {
                chatCoinAlertsRepository.save(inCollection.get());
              }
            } else {
              chatCoinAlertsRepository.save(inCollection.get());
            }
          }
          break;
        }
      }
    }
  }

  public void addPriceToChatsAlertList(int chatId, CoinSymbol symbol, float price, String msg) {
    Optional<ChatCoinAlert> inCollection = chatCoinAlertsRepository.findFirstByChatId(chatId);
    boolean foundIt = false;
    if (msg == null) {
      msg = "";
    }
    if (inCollection.isPresent()) {
      for (int i = 0; i < inCollection.get().getCoinAlertList().size(); i++) {
        if (inCollection.get().getCoinAlertList().get(i).getSymbol().equals(symbol)) {
          foundIt = true;
          List<PriceInfo> priceListInCollection = inCollection.get().getCoinAlertList().get(i).getPriceInfoList();
          if (!isInWantList(priceListInCollection, price)) {
            priceListInCollection.add(new PriceInfo(price, msg));
            chatCoinAlertsRepository.save(inCollection.get());
            break;
          }
        }
      }
    }

    if (!foundIt) {
      if (inCollection.isPresent()) {
        inCollection.get().getCoinAlertList().add(new CoinAlert(symbol, Arrays.asList(new PriceInfo(price, msg))));
        chatCoinAlertsRepository.save(inCollection.get());
      } else {
        List<CoinAlert> tmp = new ArrayList<>();
        tmp.add(new CoinAlert(symbol, Arrays.asList(new PriceInfo(price, msg))));
        chatCoinAlertsRepository.save(new ChatCoinAlert(chatId, tmp));
      }
    }
  }

  /**
   * Make it nice and add default msg = "" if no message is present
   */
  public void addMultiplePricesToChatsAlertList(int chatId, CoinSymbol symbol, List<PriceInfo> addPriceList) {
    Optional<ChatCoinAlert> inCollection = chatCoinAlertsRepository.findFirstByChatId(chatId);
    boolean needToSave = false;
    boolean foundIt = false;
    if (inCollection.isPresent()) {
      for (int i = 0; i < inCollection.get().getCoinAlertList().size(); i++) {
        if (inCollection.get().getCoinAlertList().get(i).getSymbol().equals(symbol)) {
          foundIt = true;
          List<PriceInfo> priceListInCollection = inCollection.get().getCoinAlertList().get(i).getPriceInfoList();
          for (int j = 0; j < addPriceList.size(); j++) {
            if (!isInWantList(priceListInCollection, addPriceList.get(j).getPrice())) {
              priceListInCollection.add(addPriceList.get(j));
              if (!needToSave) {
                needToSave = true;
              }
            }
          }
          if (needToSave) {
            chatCoinAlertsRepository.save(inCollection.get());
          }
          break;
        }
      }
    }
    if (!foundIt) {
      if (inCollection.isPresent()) {
        inCollection.get().getCoinAlertList().add(new CoinAlert(symbol, addPriceList));
        chatCoinAlertsRepository.save(inCollection.get());
      } else {
        List<CoinAlert> tmp = new ArrayList<>();
        tmp.add(new CoinAlert(symbol, addPriceList));
        chatCoinAlertsRepository.save(new ChatCoinAlert(chatId, tmp));
      }
    }
  }

  public void removePricesFormSameSymbol(CoinSymbol symbol, Map<Integer, List<Float>> priceMap) {
    Pageable tmpPageable = PageRequest.of(0, priceMap.keySet().size());
    List<ChatCoinAlert> inCollection = new ArrayList<>(
        chatCoinAlertsRepository.findByChatIdIn(priceMap.keySet(), tmpPageable).getContent());
    if (!inCollection.isEmpty()) {
      List<ChatCoinAlert> needToSave = new ArrayList<>();
      List<ChatCoinAlert> needToDelete = null;
      for (Entry<Integer, List<Float>> entry : priceMap.entrySet()) {
        Optional<List<CoinAlert>> thisChatsCoinAlertList = inCollection.stream()
            .filter(c -> c.getChatId() == entry.getKey()).findFirst().map(ca -> ca.getCoinAlertList());
        if (thisChatsCoinAlertList.isPresent()) {
          Optional<CoinAlert> thisChatsCoinAlert = thisChatsCoinAlertList.get().stream()
              .filter(c -> c.getSymbol().equals(symbol)).findFirst();
          if (thisChatsCoinAlert.isPresent()) {
            if (thisChatsCoinAlert.get().getPriceInfoList().removeIf(c -> entry.getValue().contains(c.getPrice()))) {
              if (thisChatsCoinAlert.get().getPriceInfoList().isEmpty()) {
                thisChatsCoinAlertList.get().remove(thisChatsCoinAlert.get());
              }
              needToSave.add(inCollection.stream().filter(c -> c.getChatId() == entry.getKey()).findFirst().get());
            }
          }
        }
        if (!needToSave.isEmpty()) {
          chatCoinAlertsRepository.saveAll(needToSave);
        }
        if (needToDelete != null) {
          chatCoinAlertsRepository.deleteAll(needToDelete);
        }
      }
    }
  }

  public ChatCoinAlert getFirstByChatId(int chatId) {
    Optional<ChatCoinAlert> inCollection = chatCoinAlertsRepository.findFirstByChatId(chatId);
    return inCollection.isPresent() ? inCollection.get() : null;
  }

  /**
   * Deprecated 
   * wantToRemoveList should already by distinct by chatId. Finds first
   * so if there are multiple later wont be removed Only going to use it in
   * checkWatchList will give db a single delete command after checking every
   */
  public void removePricesFormChatsAlertLists(List<ChatCoinAlert> toRemoveList) {
    List<Integer> idList = toRemoveList.stream().map(ChatCoinAlert::getChatId).collect(Collectors.toList());
    Pageable tmpPageable = PageRequest.of(0, idList.size());
    List<ChatCoinAlert> collectionList = new ArrayList<>(
        chatCoinAlertsRepository.findByChatIdIn(idList, tmpPageable).getContent());
    List<ChatCoinAlert> needToSaveList = new ArrayList<>();
    List<String> needToDeleteIDList = new ArrayList<>();

    if (!collectionList.isEmpty()) {
      collectionList.forEach(inC -> {
        boolean needToSave = false;
        Optional<ChatCoinAlert> cca = toRemoveList.stream().filter(c -> c.getChatId() == inC.getChatId()).findFirst();
        if (cca.isPresent()) {
          List<CoinAlert> coinAlertList = cca.get().getCoinAlertList();
          for (int i = 0; i < coinAlertList.size(); i++) {
            for (int j = 0; j < inC.getCoinAlertList().size(); j++) {
              if (inC.getCoinAlertList().get(j).getSymbol().equals(coinAlertList.get(i).getSymbol())) {
                List<PriceInfo> inCollection = inC.getCoinAlertList().get(j).getPriceInfoList();
                List<PriceInfo> wRemove = coinAlertList.get(i).getPriceInfoList();
                if (inCollection.removeIf(c -> isInWantList(wRemove, c.getPrice()))) {
                  if (!needToSave) {
                    needToSave = true;
                  }
                  if (inCollection.size() == 0) {
                    inC.getCoinAlertList().remove(j);
                  }
                }
                break;
              }
            }
          }
        }
        if (needToSave) {
          if (inC.getCoinAlertList().isEmpty()) {
            needToDeleteIDList.add(inC.getId());
          } else {
            needToSaveList.add(inC);
          }
        }
      });
    }
    if (!needToSaveList.isEmpty()) {
      chatCoinAlertsRepository.saveAll(needToSaveList);
    }
    if (!needToDeleteIDList.isEmpty()) {
      chatCoinAlertsRepository.deleteAllByIdIn(needToDeleteIDList);
    }
  }

  /**
   * Deprecated
   */
  public void addPricesToChatsAlertLists(List<ChatCoinAlert> toAddList) {
    List<Integer> idList = toAddList.stream().map(ChatCoinAlert::getChatId).collect(Collectors.toList());
    Pageable tmpPageable = PageRequest.of(0, idList.size());
    List<ChatCoinAlert> collectionList = new ArrayList<>(
        chatCoinAlertsRepository.findByChatIdIn(idList, tmpPageable).getContent());

    List<ChatCoinAlert> needToSaveList = new ArrayList<>();

    if (!collectionList.isEmpty()) {
      collectionList.forEach(inC -> {
        boolean needToSave = false;
        Optional<ChatCoinAlert> cca = toAddList.stream().filter(c -> c.getChatId() == inC.getChatId()).findFirst();
        if (cca.isPresent()) {
          List<CoinAlert> coinAlertList = cca.get().getCoinAlertList();
          for (int i = 0; i < coinAlertList.size(); i++) {
            for (int j = 0; j < inC.getCoinAlertList().size(); j++) {
              if (inC.getCoinAlertList().get(j).getSymbol().equals(coinAlertList.get(i).getSymbol())) {
                List<PriceInfo> inCollection = inC.getCoinAlertList().get(j).getPriceInfoList();
                List<PriceInfo> toAddListTmp = coinAlertList.get(i).getPriceInfoList();
                List<PriceInfo> needToAdd = toAddListTmp.stream().filter(c -> !isInWantList(inCollection, c.getPrice()))
                    .collect(Collectors.toList());
                if (!needToAdd.isEmpty()) {
                  inCollection.addAll(needToAdd);
                  if (!needToSave) {
                    needToSave = true;
                  }
                }
                break;
              }
            }
          }
        }
        if (needToSave) {
          needToSaveList.add(inC);
        }
      });
    }

    if (collectionList.size() != toAddList.size()) {
      for (int i = 0; i < toAddList.size(); i++) {
        boolean brandNewChat = true;
        for (int j = 0; j < collectionList.size(); j++) {
          if (collectionList.get(j).getChatId() == toAddList.get(i).getChatId()) {
            brandNewChat = false;
            break;
          }
        }
        if (brandNewChat) {
          needToSaveList.add(new ChatCoinAlert(toAddList.get(i).getChatId(), toAddList.get(i).getCoinAlertList()));
        }
      }
    }

    if (!needToSaveList.isEmpty()) {
      chatCoinAlertsRepository.saveAll(needToSaveList);
    }
  }

  /**
   * Deprecated
   */
  public void removePriceFromChatsAlertList(int chatId, CoinSymbol symbol, float price) {
    Optional<ChatCoinAlert> inCollection = chatCoinAlertsRepository.findFirstByChatId(chatId);
    if (inCollection.isPresent()) {
      for (int i = 0; i < inCollection.get().getCoinAlertList().size(); i++) {
        if (inCollection.get().getCoinAlertList().get(i).getSymbol().equals(symbol)) {
          List<PriceInfo> priceList = inCollection.get().getCoinAlertList().get(i).getPriceInfoList();
          for (int j = 0; j < priceList.size(); j++) {
            if (priceList.get(j).getPrice() == price) {
              priceList.remove(j);
              if (priceList.isEmpty()) {
                inCollection.get().getCoinAlertList().remove(i);
                if (inCollection.get().getCoinAlertList().isEmpty()) {
                  chatCoinAlertsRepository.deleteById(inCollection.get().getId());
                } else {
                  chatCoinAlertsRepository.save(inCollection.get());
                }
                break;
              }
              chatCoinAlertsRepository.save(inCollection.get());
              break;
            }
          }
          break;
        }
      }
    }
  }

}
