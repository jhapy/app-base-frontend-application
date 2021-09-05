/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.components.card;

import com.github.appreciated.card.StatefulCard;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StatefulCardGroup<T extends StatefulCard> extends Composite<VerticalLayout> {

  private StatefulCard currentFocus;
  private Consumer<T> listener;
  private final List<T> cards = new ArrayList<>();

  @SafeVarargs
  public StatefulCardGroup(T... cards) {
    add(cards);
    getContent().setMargin(false);
    getContent().setPadding(false);
    getContent().setSpacing(false);
  }

  @SafeVarargs
  public final void add(T... cards) {
    getContent().add(cards);
    getContent().add(cards);
    Arrays.stream(cards).forEach(card -> card.addClickListener(event -> setState(card)));
    this.cards.addAll(Arrays.asList(cards));
  }

  public void removeAll() {
    getContent().removeAll();
    this.cards.clear();
  }

  public void setState(T nextFocus, boolean notifyListeners) {
    if (nextFocus != currentFocus) {
      nextFocus.setSelected(true);
      if (currentFocus != null) {
        currentFocus.setSelected(false);
      }
      currentFocus = nextFocus;
      if (listener != null && notifyListeners) {
        listener.accept(nextFocus);
      }
    }
  }

  public StatefulCard getState() {
    return currentFocus;
  }

  public void setState(T nextFocus) {
    setState(nextFocus, true);
  }

  public StatefulCardGroup<T> withStateChangedListener(Consumer<T> listener) {
    setStateChangedListener(listener);
    return this;
  }

  public void setStateChangedListener(Consumer<T> listener) {
    this.listener = listener;
  }

  public void setHighlight(boolean enabled) {
    if (enabled) {
      getContent().getStyle().remove("--card-state-highlight");
    } else {
      getContent().getStyle().set("--card-state-highlight", "transparent");
    }
  }

  public List<T> getCards() {
    return cards;
  }
}