package com.su.Model;

import org.springframework.stereotype.Component;

@Component
public class AllSleep {
  private boolean isSleep = false;

  public boolean getIsSleep() {
    return this.isSleep;
  }

  public void setIsSleep(boolean isSleep) {
    this.isSleep = isSleep;
  }

}
