package com.faforever.client;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;

public class TooltipMonitor {

  Set<Node> allLabels = Collections.newSetFromMap(new WeakHashMap<Node, Boolean>());
  Set<Node> allInnerLabels = Collections.newSetFromMap(new WeakHashMap<Node, Boolean>());
  
  public void iterate(Scene scene) {
    Parent p = scene.getRoot();
    
    MutableInt labelsCounter = new MutableInt(0);
    Consumer<Node> found = node -> {
      labelsCounter.increment();
      allLabels.add(node);
    };
    MutableInt innerLabelsCounter = new MutableInt(0);
    Consumer<Node> innerFound = node -> {
      innerLabelsCounter.increment();
      allInnerLabels.add(node);
    };
    
    walkScenegraph(p, found, innerFound);
    long threadId = Thread.currentThread().getId();
    System.out.println(threadId + ":"
        + " [current: " + labelsCounter.intValue() + ", all: " + allLabels.size() + "]"
        + " [current: " + innerLabelsCounter.intValue() + ", all: " + allInnerLabels.size() + "]");
  }
  
  public <T extends Node> void walkScenegraph(Node node, Consumer<Node> foundNodes, Consumer<Node> tooltipNodes) {
    if (node == null) {
      return;
    } else if (node instanceof Label) {
      Label label = (Label) node;
      foundNodes.accept(label);
      if (tooltipNodes != null) {
        Tooltip innerTooltip = label.getTooltip();
//        innerTooltip.getId().equals(anObject)
        if (innerTooltip != null) {
          Scene innerScene = innerTooltip.getScene();
          if (innerScene != null) {
            walkScenegraph(innerScene.getRoot(), tooltipNodes, null);
          }
        }
      }
    } else if (node instanceof TitledPane) {
      TitledPane tPane = (TitledPane) node;
      walkScenegraph(tPane.getContent(), foundNodes, tooltipNodes);
    } else if (node instanceof ScrollPane) {
      ScrollPane sPane = (ScrollPane) node;
      walkScenegraph(sPane.getContent(), foundNodes, tooltipNodes);
    } else if (node instanceof Parent) {
      Parent parent = (Parent) node;
      parent.getChildrenUnmodifiable().forEach(child -> walkScenegraph(child, foundNodes, tooltipNodes));
    }
  }
}
