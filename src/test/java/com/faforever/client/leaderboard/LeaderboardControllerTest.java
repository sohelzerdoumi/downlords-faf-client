package com.faforever.client.leaderboard;

import com.faforever.client.chat.avatar.AvatarService;
import com.faforever.client.game.KnownFeaturedMod;
import com.faforever.client.i18n.I18n;
import com.faforever.client.leaderboard.LeaderboardController.League;
import com.faforever.client.main.event.OpenRanked1v1LeaderboardEvent;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.player.PlayerService;
import com.faforever.client.reporting.ReportingService;
import com.faforever.client.test.AbstractPlainJavaFxTest;
import com.faforever.client.theme.UiService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class LeaderboardControllerTest extends AbstractPlainJavaFxTest {

  private LeaderboardController instance;

  @Mock
  private LeaderboardService leaderboardService;
  @Mock
  private UiService uiService;
  @Mock
  private PlayerService playerService;
  @Mock
  private I18n i18n;
  @Mock
  private AvatarService avatarService;
  @Mock
  private Division division;

  @Before
  //TODO These need rewrites
  public void setUp() throws Exception {
    instance = new LeaderboardController(leaderboardService, i18n, playerService, uiService, avatarService);

    loadFxml("theme/leaderboard/leaderboard.fxml", clazz -> instance);
  }

  @Test
  @Ignore
  public void testOnDisplay() throws Exception {
    when(leaderboardService.getEntries(division)).thenReturn(CompletableFuture.completedFuture(Arrays.asList(
        new LeaderboardEntry(), new LeaderboardEntry()
    )));

    CountDownLatch loadedLatch = new CountDownLatch(1);
    instance.ratingTable.itemsProperty().addListener(observable -> loadedLatch.countDown());
    instance.setLeagueType(KnownFeaturedMod.LADDER_1V1);

    instance.display(new OpenRanked1v1LeaderboardEvent());

    assertTrue(loadedLatch.await(3, TimeUnit.SECONDS));
    verifyZeroInteractions(notificationService);
  }

  @Test
  @Ignore
  public void testFilterByNamePlayerExactMatch() throws Exception {
    LeaderboardEntry entry1 = new LeaderboardEntry();
    entry1.setUsername("Aa");
    LeaderboardEntry entry2 = new LeaderboardEntry();
    entry2.setUsername("Ab");

    when(leaderboardService.getEntries()).thenReturn(CompletableFuture.completedFuture(Arrays.asList(
        entry1, entry2
    )));
    instance.setLeagueType(League.RANKED1V1);
    instance.display(new OpenRanked1v1LeaderboardEvent());

    assertThat(instance.ratingTable.getSelectionModel().getSelectedItem(), nullValue());

    instance.searchTextField.setText("aa");
    assertThat(instance.ratingTable.getItems(), hasSize(2));
    assertThat(instance.ratingTable.getSelectionModel().getSelectedItem().getUsername(), is("Aa"));
  }

  @Test
  @Ignore
  public void testFilterByNamePlayerPartialMatch() throws Exception {
    LeaderboardEntry entry1 = new LeaderboardEntry();
    entry1.setUsername("Aa");
    LeaderboardEntry entry2 = new LeaderboardEntry();
    entry2.setUsername("Ab");

    when(leaderboardService.getEntries()).thenReturn(CompletableFuture.completedFuture(Arrays.asList(
        entry1, entry2
    )));
    instance.setLeagueType(League.RANKED1V1);
    instance.display(new OpenRanked1v1LeaderboardEvent());

    assertThat(instance.ratingTable.getSelectionModel().getSelectedItem(), nullValue());

    instance.searchTextField.setText("b");
    assertThat(instance.ratingTable.getItems(), hasSize(2));
    assertThat(instance.ratingTable.getSelectionModel().getSelectedItem().getUsername(), is("Ab"));
  }

  @Test
  public void testGetRoot() throws Exception {
    assertThat(instance.getRoot(), is(instance.leaderboardRoot));
    assertThat(instance.getRoot().getParent(), is(nullValue()));
  }
}
