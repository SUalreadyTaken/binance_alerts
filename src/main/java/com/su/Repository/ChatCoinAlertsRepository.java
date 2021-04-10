package com.su.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.su.Model.ChatCoinAlert;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatCoinAlertsRepository extends MongoRepository<ChatCoinAlert, String> {
  Optional<ChatCoinAlert> findFirstByChatId(int chatId);
  void deleteByIdIn(List<String> idList);
  void deleteAllByIdIn(List<String> idList);
  Slice<ChatCoinAlert> findByChatIdIn(List<Integer> chatIds, Pageable pageable);
  Slice<ChatCoinAlert> findByChatIdIn(Set<Integer> chatIds, Pageable pageable);
}
