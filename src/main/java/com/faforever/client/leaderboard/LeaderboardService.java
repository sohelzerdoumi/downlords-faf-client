package com.faforever.client.leaderboard;

import com.faforever.client.leaderboard.LeaderboardController.League;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LeaderboardService {
  int MINIMUM_GAMES_PLAYED_TO_BE_SHOWN = 10;

  CompletableFuture<List<RatingStat>> getLadder1v1Stats();

  CompletableFuture<LeaderboardEntry> getEntryForPlayer(int playerId);

  CompletableFuture<LeaderboardEntry> getLeagueEntryForPlayer(int playerId, League leagueType);

  CompletableFuture<List<LeaderboardEntry>> getEntries(Division division);

  CompletableFuture<List<DivisionStat>> getDivisionStats();

  CompletableFuture<List<Division>> getDivisions(League leagueType);

  CompletableFuture<List<LeaderboardEntry>> getDivisionEntries(Division division);
}
