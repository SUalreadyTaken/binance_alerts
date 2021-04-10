package com.su.Model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

@Component
public class TelegramMessageQueue {
  BlockingQueue<TelegramInput> messageQueue = new LinkedBlockingQueue<>();

  public BlockingQueue<TelegramInput> getMessageQueue() {
    return this.messageQueue;
  }
}
