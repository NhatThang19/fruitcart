package com.vn.fruitcart.util.constant;

public enum GenderEnum {
  MALE("Nam"), FEMALE("Nữ");

  private final String displayName;

  GenderEnum(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
