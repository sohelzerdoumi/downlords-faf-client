package com.faforever.client.leaderboard;

import com.faforever.client.api.dto.DivisionName;
import com.faforever.client.leaderboard.LeaderboardController.League;
import lombok.Value;

@Value
public class Division {
  int leagueSeasonId;
  int majorDivisionIndex;
  int subDivisionIndex;
  DivisionName majorDivisionName;
  DivisionName subDivisionName;
  int highestScore;
  League leagueType;
}
