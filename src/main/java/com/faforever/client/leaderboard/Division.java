package com.faforever.client.leaderboard;

import com.faforever.client.api.dto.DivisionName;
import lombok.Value;

@Value
public class Division {
  int leagueSeasonId;
  int majorDivisionIndex;
  int subDivisionIndex;
  DivisionName majorDivisionName;
  DivisionName subDivisionName;
  int highestScore;
}
