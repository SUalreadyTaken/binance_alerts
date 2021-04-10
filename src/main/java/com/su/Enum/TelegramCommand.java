package com.su.Enum;

public enum TelegramCommand {
  STATUS("STATUS"),
  ADD("ADD"),
  REMOVE("REMOVE"),
  ADDT("ADDT"),
  PRICE("PRICE"),
  EDIT("EDIT");
  
  public final String label;

  private TelegramCommand(String label) {
    this.label = label;
  }
}
