package com.faforever.client.replay;

import com.faforever.client.api.dto.Game;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.i18n.I18n;
import com.faforever.client.main.event.NavigateEvent;
import com.faforever.client.main.event.OpenOnlineReplayVaultEvent;
import com.faforever.client.main.event.ShowReplayEvent;
import com.faforever.client.main.event.ShowUserReplaysEvent;
import com.faforever.client.mod.FeaturedMod;
import com.faforever.client.mod.ModService;
import com.faforever.client.notification.ImmediateNotification;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.notification.Severity;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.query.CategoryFilterController;
import com.faforever.client.query.DateRangeFilterController;
import com.faforever.client.query.RangeFilterController;
import com.faforever.client.query.SearchablePropertyMappings;
import com.faforever.client.query.TextFilterController;
import com.faforever.client.query.ToggleFilterController;
import com.faforever.client.reporting.ReportingService;
import com.faforever.client.theme.UiService;
import com.faforever.client.vault.VaultEntityController;
import com.faforever.client.vault.search.SearchController.SearchConfig;
import com.faforever.client.vault.search.SearchController.SortConfig;
import com.faforever.client.vault.search.SearchController.SortOrder;
import javafx.application.Platform;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OnlineReplayVaultController extends VaultEntityController<Replay> {

  private static final int TOP_ELEMENT_COUNT = 6;

  private final ModService modService;
  private final ReplayService replayService;

  private int playerId;
  private ReplayDetailController replayDetailController;

  public OnlineReplayVaultController(ModService modService, ReplayService replayService, UiService uiService, NotificationService notificationService, I18n i18n, PreferencesService preferencesService, ReportingService reportingService) {
    super(uiService, notificationService, i18n, preferencesService, reportingService);
    this.replayService = replayService;
    this.modService = modService;
  }

  @Override
  public void initialize() {
    super.initialize();
    uploadButton.setVisible(false);
  }

  @Override
  protected void onDisplayDetails(Replay replay) {
    JavaFxUtil.assertApplicationThread();
    replayDetailController.setReplay(replay);
    replayDetailController.getRoot().setVisible(true);
    replayDetailController.getRoot().requestFocus();
  }

  protected void setSupplier(SearchConfig searchConfig) {
    switch (searchType) {
      case SEARCH:
        currentSupplier = replayService.findByQueryWithPageCount(searchConfig.getSearchQuery(), pageSize, pagination.getCurrentPageIndex() + 1, searchConfig.getSortConfig());
        break;
      case OWN:
        currentSupplier = replayService.getOwnReplaysWithPageCount(pageSize, pagination.getCurrentPageIndex() + 1);
        break;
      case NEWEST:
        currentSupplier = replayService.getNewestReplaysWithPageCount(pageSize, pagination.getCurrentPageIndex() + 1);
        break;
      case HIGHEST_RATED:
        currentSupplier = replayService.getHighestRatedReplaysWithPageCount(pageSize, pagination.getCurrentPageIndex() + 1);
        break;
      case PLAYER:
        currentSupplier = replayService.getReplaysForPlayerWithPageCount(playerId, pageSize, pagination.getCurrentPageIndex() + 1, new SortConfig("startTime", SortOrder.DESC));
        break;
    }
  }

  protected Node getEntityCard(Replay replay) {
    ReplayCardController controller = uiService.loadFxml("theme/vault/replay/replay_card.fxml");
    controller.setReplay(replay);
    controller.setOnOpenDetailListener(this::onDisplayDetails);
    return controller.getRoot();
  }

  @Override
  protected List<ShowRoomCategory> getShowRoomCategories() {
    return Arrays.asList(
        new ShowRoomCategory(() -> replayService.getOwnReplaysWithPageCount(TOP_ELEMENT_COUNT, 1), SearchType.OWN, "vault.replays.ownReplays"),
        new ShowRoomCategory(() -> replayService.getNewestReplaysWithPageCount(TOP_ELEMENT_COUNT, 1), SearchType.NEWEST, "vault.replays.newest"),
        new ShowRoomCategory(() -> replayService.getHighestRatedReplaysWithPageCount(TOP_ELEMENT_COUNT, 1), SearchType.HIGHEST_RATED, "vault.replays.highestRated")
    );
  }

  public void onUploadButtonClicked() {
    //do nothing
  }

  @Override
  protected Node getDetailView() {
    replayDetailController = uiService.loadFxml("theme/vault/replay/replay_detail.fxml");
    return replayDetailController.getRoot();
  }

  protected void initSearchController() {
    searchController.setRootType(Game.class);
    searchController.setSearchableProperties(SearchablePropertyMappings.GAME_PROPERTY_MAPPING);
    searchController.setSortConfig(preferencesService.getPreferences().getVaultPrefs().onlineReplaySortConfigProperty());
    searchController.setOnlyShowLastYearCheckBoxVisible(true);
    searchController.setVaultRoot(vaultRoot);
    searchController.setSavedQueries(preferencesService.getPreferences().getVaultPrefs().getSavedReplayQueries());

    TextFilterController playerFilterController = uiService.loadFxml("theme/vault/search/textFilter.fxml");
    playerFilterController.setPropertyName("playerStats.player.login");
    playerFilterController.setTitle(i18n.get("game.player.username"));
    playerFilterController.setOnAction(() -> searchController.onSearchButtonClicked());
    searchController.addFilterNode(playerFilterController);

    TextFilterController mapNameFilterController = uiService.loadFxml("theme/vault/search/textFilter.fxml");
    mapNameFilterController.setPropertyName("mapVersion.map.displayName");
    mapNameFilterController.setTitle(i18n.get("game.map.displayName"));
    mapNameFilterController.setOnAction(() -> searchController.onSearchButtonClicked());
    searchController.addFilterNode(mapNameFilterController);

    TextFilterController mapAuthorFilterController = uiService.loadFxml("theme/vault/search/textFilter.fxml");
    mapAuthorFilterController.setPropertyName("mapVersion.map.author.login");
    mapAuthorFilterController.setTitle(i18n.get("game.map.author"));
    mapAuthorFilterController.setOnAction(() -> searchController.onSearchButtonClicked());
    searchController.addFilterNode(mapAuthorFilterController);

    TextFilterController gameNameFilterController = uiService.loadFxml("theme/vault/search/textFilter.fxml");
    gameNameFilterController.setPropertyName("name");
    gameNameFilterController.setTitle(i18n.get("game.title"));
    gameNameFilterController.setOnAction(() -> searchController.onSearchButtonClicked());
    searchController.addFilterNode(gameNameFilterController);

    TextFilterController gameIDFilterController = uiService.loadFxml("theme/vault/search/textFilter.fxml");
    gameIDFilterController.setPropertyName("id");
    gameIDFilterController.setTitle(i18n.get("game.id"));
    gameIDFilterController.setOnAction(() -> searchController.onSearchButtonClicked());
    searchController.addFilterNode(gameIDFilterController);

    CategoryFilterController featuredModFilterController = uiService.loadFxml("theme/vault/search/categoryFilter.fxml");
    featuredModFilterController.setTitle(i18n.get("featuredMod.displayName"));
    featuredModFilterController.setPropertyName("featuredMod.displayName");
    searchController.addFilterNode(featuredModFilterController);

    modService.getFeaturedMods().thenAccept(featuredMods ->
        Platform.runLater(() ->
            featuredModFilterController.setItems(featuredMods.stream().map(FeaturedMod::getDisplayName)
                .collect(Collectors.toList()))));

    RangeFilterController ladderRatingRangeFilterController = uiService.loadFxml("theme/vault/search/rangeFilter.fxml");
    ladderRatingRangeFilterController.setTitle(i18n.get("game.ladderRating"));
    ladderRatingRangeFilterController.setPropertyName("playerStats.player.ladder1v1Rating.rating");
    ladderRatingRangeFilterController.setMin(0.0);
    ladderRatingRangeFilterController.setMax(3000.0);
    ladderRatingRangeFilterController.setIncrement(100.0);
    ladderRatingRangeFilterController.setTickUnit(100.0);
    ladderRatingRangeFilterController.setSnapToTicks(true);
    searchController.addFilterNode(ladderRatingRangeFilterController);

    RangeFilterController globalRatingRangeFilterController = uiService.loadFxml("theme/vault/search/rangeFilter.fxml");
    globalRatingRangeFilterController.setTitle(i18n.get("game.globalRating"));
    globalRatingRangeFilterController.setPropertyName("playerStats.player.globalRating.rating");
    globalRatingRangeFilterController.setMin(0.0);
    globalRatingRangeFilterController.setMax(3000.0);
    globalRatingRangeFilterController.setIncrement(100.0);
    globalRatingRangeFilterController.setTickUnit(100.0);
    globalRatingRangeFilterController.setSnapToTicks(true);
    searchController.addFilterNode(globalRatingRangeFilterController);

    DateRangeFilterController dateRangeFilterController = uiService.loadFxml("theme/vault/search/dateRangeFilter.fxml");
    dateRangeFilterController.setTitle(i18n.get("game.date"));
    dateRangeFilterController.setPropertyName("endTime");
    dateRangeFilterController.setInitialYearsBefore(1);
    searchController.addFilterNode(dateRangeFilterController);

    ToggleFilterController validityFilterController = uiService.loadFxml("theme/vault/search/toggleFilter.fxml");
    validityFilterController.setTitle(i18n.get("game.onlyRanked"));
    validityFilterController.setPropertyName("validity");
    validityFilterController.setValue("VALID");
    searchController.addFilterNode(validityFilterController);
  }

  @Override
  protected Class<? extends NavigateEvent> getDefaultNavigateEvent() {
    return OpenOnlineReplayVaultEvent.class;
  }

  @Override
  protected void handleSpecialNavigateEvent(NavigateEvent navigateEvent) {
    if (navigateEvent instanceof ShowReplayEvent) {
      onShowReplayEvent((ShowReplayEvent) navigateEvent);
    } else if (navigateEvent instanceof ShowUserReplaysEvent) {
      onShowUserReplaysEvent((ShowUserReplaysEvent) navigateEvent);
    } else {
      log.warn("No such NavigateEvent for this Controller: {}", navigateEvent.getClass());
    }
  }

  private void onShowReplayEvent(ShowReplayEvent event) {
    int replayId = event.getReplayId();
    replayService.findById(replayId).thenAccept(replay -> {
      if (replay.isPresent()) {
        Platform.runLater(() -> onDisplayDetails(replay.get()));
      } else {
        notificationService.addNotification(new ImmediateNotification(i18n.get("replay.notFoundTitle"), i18n.get("replay.replayNotFoundText", replayId), Severity.WARN));
      }
    });
  }

  private void onShowUserReplaysEvent(ShowUserReplaysEvent event) {
    enterSearchingState();
    searchType = SearchType.PLAYER;
    playerId = event.getPlayerId();
    SortConfig sortConfig = new SortConfig("startTime", SortOrder.DESC);
    displayFromSupplier(() -> replayService.getReplaysForPlayerWithPageCount(playerId, pageSize, 1, sortConfig), true);
  }
}
