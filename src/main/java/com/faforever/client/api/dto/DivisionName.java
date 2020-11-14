package com.faforever.client.api.dto;

import lombok.Getter;

@Getter
public enum DivisionName {
  BRONZE("leagues.divisionName.bronze", "Bronze"),
  SILVER("leagues.divisionName.silver", "Silver"),
  GOLD("leagues.divisionName.gold", "Gold"),
  DIAMOND("leagues.divisionName.diamond", "Diamond"),
  MASTER("leagues.divisionName.master", "Master"),
  COMMANDER("leagues.divisionName.commander", "Grandmaster"),
  I("leagues.divisionName.I", "1"),
  II("leagues.divisionName.II", "2"),
  III("leagues.divisionName.III", "3"),
  IV("leagues.divisionName.IV", "4"),
  V("leagues.divisionName.V", "5"),
  NONE("leagues.divisionName.none", "");

  private final String i18nKey;
  private final String imageKey;

  DivisionName(String i18nKey, String imageKey) {
    this.i18nKey = i18nKey;
    this.imageKey = imageKey;
  }
}
