package com.su.Model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

@Component
public class DBQueue {
  BlockingQueue<DbInput> dbQueue = new LinkedBlockingQueue<>();

  public BlockingQueue<DbInput> getDbQueue() {
    return this.dbQueue;
  }
  
}
