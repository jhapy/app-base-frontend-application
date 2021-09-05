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

package org.jhapy.frontend.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.function.ValueProvider;

/**
 * A grid component for displaying hierarchical tabular data.
 *
 * @param <T> the grid bean type
 * @author Vaadin Ltd
 */
public class MyTreeGrid<T> extends TreeGrid<T> {

  /**
   * Adds a new Hierarchy column to this {@link Grid} with a value provider. The value is converted
   * to String when sent to the client by using {@link String#valueOf(Object)}.
   *
   * <p>Hierarchy column is rendered by using 'vaadin-grid-tree-toggle' web component.
   *
   * @param valueProvider the value provider
   * @return the created hierarchy column
   */
  public Column<T> addHierarchyColumn(ValueProvider<T, ?> valueProvider) {
    /*
    column.setComparator(
            ((a, b) -> compareMaybeComparables(valueProvider.apply(a),
                    valueProvider.apply(b))));
    */
    return addColumn(
        TemplateRenderer.<T>of(
                "<vaadin-grid-tree-toggle "
                    + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>[[item.name]]"
                    + "</vaadin-grid-tree-toggle>")
            .withProperty("leaf", item -> !getDataCommunicator().hasChildren(item))
            .withProperty("name", value -> String.valueOf(valueProvider.apply(value))));
  }
}