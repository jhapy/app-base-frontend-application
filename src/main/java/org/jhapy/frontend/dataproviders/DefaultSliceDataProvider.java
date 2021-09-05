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

package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.utils.DirectionEnum;
import org.jhapy.dto.utils.Slice;
import org.jhapy.frontend.dataproviders.utils.FilterableSliceDataProvider;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class DefaultSliceDataProvider<T extends BaseEntity, F extends DefaultFilter>
    extends FilterableSliceDataProvider<T, F> implements Serializable {

  private List<QuerySortOrder> defaultSortOrder;
  private Consumer<Slice<T>> pageObserver;
  private Query<T, F> currentQuery;

  public DefaultSliceDataProvider(DirectionEnum defaultSortDirection, String[] defaultSortFields) {
    setSortOrder(defaultSortDirection, defaultSortFields);
  }

  private void setSortOrder(DirectionEnum direction, String[] properties) {
    QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
    for (String property : properties) {
      if (direction.equals(DirectionEnum.ASC)) {
        builder.thenAsc(property);
      } else {
        builder.thenDesc(property);
      }
    }
    defaultSortOrder = builder.build();
  }

  public Query<T, F> getCurrentQuery() {
    return currentQuery;
  }

  public void setCurrentQuery(Query<T, F> currentQuery) {
    this.currentQuery = currentQuery;
  }

  @Override
  protected List<QuerySortOrder> getDefaultSortOrders() {
    return defaultSortOrder;
  }

  public Consumer<Slice<T>> getPageObserver() {
    return pageObserver;
  }

  public void setPageObserver(Consumer<Slice<T>> pageObserver) {
    this.pageObserver = pageObserver;
  }

  @Override
  public Object getId(T item) {
    return item.getId();
  }
}