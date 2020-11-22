package com.faforever.client.vault;

import com.faforever.client.fx.AbstractViewController;
import com.faforever.client.main.event.NavigateEvent;
import com.faforever.client.main.event.OpenMapVaultEvent;
import com.faforever.client.main.event.OpenModVaultEvent;
import com.faforever.client.map.MapVaultController;
import com.faforever.client.mod.ModVaultController;
import com.faforever.client.theme.UiService;
import com.google.common.eventbus.EventBus;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VaultController extends AbstractViewController<Node> {
  // TODO change to spring event bus
  private final EventBus eventBus;
  private final UiService uiService;
  public TabPane vaultRoot;
  public Tab mapVaultTab;
  public Tab modVaultTab;
  public MapVaultController mapVaultController;
  public ModVaultController modVaultController;

  private boolean isHandlingEvent;
  private AbstractViewController<?> lastTabController;
  private Tab lastTab;

  public VaultController(EventBus eventBus, UiService uiService) {
    this.eventBus = eventBus;
    this.uiService = uiService;
  }

  @Override
  public Node getRoot() {
    return vaultRoot;
  }

  @Override
  public void initialize() {
    mapVaultController = uiService.loadFxml("theme/vault/vault_entity.fxml", MapVaultController.class);
    mapVaultTab.setContent(mapVaultController.getRoot());
    modVaultController = uiService.loadFxml("theme/vault/vault_entity.fxml", ModVaultController.class);
    modVaultTab.setContent(modVaultController.getRoot());
    lastTab = mapVaultTab;
    lastTabController = mapVaultController;
    vaultRoot.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (isHandlingEvent) {
        return;
      }

      if (newValue == mapVaultTab) {
        eventBus.post(new OpenMapVaultEvent());
      } else if (newValue == modVaultTab) {
        eventBus.post(new OpenModVaultEvent());
      }
      // TODO implement other tabs
    });
  }

  @Override
  protected void onDisplay(NavigateEvent navigateEvent) {
    isHandlingEvent = true;

    try {
      if (navigateEvent instanceof OpenMapVaultEvent) {
        lastTab = mapVaultTab;
        lastTabController = mapVaultController;
      } else if (navigateEvent instanceof OpenModVaultEvent) {
        lastTab = modVaultTab;
        lastTabController = modVaultController;
      }
      vaultRoot.getSelectionModel().select(lastTab);
      lastTabController.display(navigateEvent);
    } finally {
      isHandlingEvent = false;
    }
  }
}
