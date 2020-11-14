package com.faforever.client.leaderboard;

import com.faforever.client.fx.AbstractViewController;
import com.faforever.client.leaderboard.LeaderboardController.League;
import com.faforever.client.main.event.NavigateEvent;
import com.faforever.client.main.event.OpenRanked2v2LeaderboardEvent;
import com.faforever.client.main.event.OpenRanked1v1LeaderboardEvent;
import com.faforever.client.main.event.OpenTeamLeaderboardEvent;
import com.google.common.eventbus.EventBus;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LeaderboardsController extends AbstractViewController<Node> {
  private final EventBus eventBus;

  public TabPane leaderboardRoot;
  public LeaderboardController ranked1v1LeaderboardController;
  public LeaderboardController ranked2v2LeaderboardController;
  public LeaderboardController teamLeaderboardController;
  public Tab ranked1v1LeaderboardTab;
  public Tab ranked2v2LeaderboardTab;
  public Tab teamLeaderboardTab;

  private boolean isHandlingEvent;
  private AbstractViewController<?> lastTabController;
  private Tab lastTab;

  public LeaderboardsController(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public Node getRoot() {
    return leaderboardRoot;
  }

  @Override
  public void initialize() {
    lastTab = ranked1v1LeaderboardTab;
    lastTabController = ranked1v1LeaderboardController;
    ranked1v1LeaderboardController.setLeagueType(League.RANKED1V1);
    ranked2v2LeaderboardController.setLeagueType(League.RANKED2V2);
    teamLeaderboardController.setLeagueType(League.TEAM);

    leaderboardRoot.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (isHandlingEvent) {
        return;
      }

      if (newValue == ranked1v1LeaderboardTab) {
        eventBus.post(new OpenRanked1v1LeaderboardEvent());
      } else if (newValue == ranked2v2LeaderboardTab) {
        eventBus.post(new OpenRanked2v2LeaderboardEvent());
      } else if (newValue == teamLeaderboardTab) {
        eventBus.post(new OpenTeamLeaderboardEvent());
      }
    });
  }

  @Override
  protected void onDisplay(NavigateEvent navigateEvent) {
    isHandlingEvent = true;

    try {
      if (navigateEvent instanceof OpenRanked1v1LeaderboardEvent) {
        lastTab = ranked1v1LeaderboardTab;
        lastTabController = ranked1v1LeaderboardController;
      } else if (navigateEvent instanceof OpenRanked2v2LeaderboardEvent) {
        lastTab = ranked2v2LeaderboardTab;
        lastTabController = ranked2v2LeaderboardController;
      } else if (navigateEvent instanceof OpenTeamLeaderboardEvent) {
        lastTab = teamLeaderboardTab;
        lastTabController = teamLeaderboardController;
      }
      leaderboardRoot.getSelectionModel().select(lastTab);
      lastTabController.display(navigateEvent);
    } finally {
      isHandlingEvent = false;
    }
  }
}
