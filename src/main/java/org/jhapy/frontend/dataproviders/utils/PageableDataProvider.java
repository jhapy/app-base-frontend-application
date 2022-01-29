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

package org.jhapy.frontend.dataproviders.utils;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.dto.utils.Pageable.Order;
import org.jhapy.dto.utils.Pageable.Order.Direction;
import org.jhapy.frontend.utils.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-03
 */
public abstract class PageableDataProvider<T extends Serializable, F>
    extends AbstractBackEndDataProvider<T, F> {

  private static Order queryOrderToSpringOrder(QuerySortOrder queryOrder) {
    return new Order(
        queryOrder.getDirection() == SortDirection.ASCENDING ? Direction.ASC : Direction.DESC,
        queryOrder.getSorted());
  }

  public static Pair<Integer, Integer> limitAndOffsetToPageSizeAndNumber(int offset, int limit) {
    int lastIndex = offset + limit - 1;
    int maxPageSize = lastIndex + 1;

    for (double pageSize = limit; pageSize <= maxPageSize; pageSize++) {
      int startPage = (int) (offset / pageSize);
      int endPage = (int) (lastIndex / pageSize);
      if (startPage == endPage) {
        // It fits on one page, let's go with that
        return Pair.of((int) pageSize, startPage);
      }
    }

    // Should not really get here
    return Pair.of(maxPageSize, 0);
  }

  @Override
  protected Stream<T> fetchFromBackEnd(Query<T, F> query) {
    Pageable pageable = getPageable(query);
    PageDTO<T> result = fetchFromBackEnd(query, pageable);
    return fromPageable(result, pageable, query);
  }

  protected abstract PageDTO<T> fetchFromBackEnd(Query<T, F> query, Pageable pageable);

  private Pageable getPageable(Query<T, F> q) {

    Pair<Integer, Integer> pageSizeAndNumber =
        limitAndOffsetToPageSizeAndNumber(q.getOffset(), q.getLimit());

    return new Pageable(
        pageSizeAndNumber.getSecond(),
        pageSizeAndNumber.getFirst(),
        q.getOffset(),
        createSpringSort(q));
  }

  private <T, F> Collection<Order> createSpringSort(Query<T, F> q) {
    List<QuerySortOrder> sortOrders;
    if (q.getSortOrders() == null || q.getSortOrders().isEmpty()) {
      sortOrders = getDefaultSortOrders();
    } else {
      sortOrders = q.getSortOrders();
    }
    List<Order> orders =
        sortOrders.stream()
            .map(PageableDataProvider::queryOrderToSpringOrder)
            .collect(Collectors.toList());
    if (orders.isEmpty()) {
      return null;
    } else {
      return new ArrayList<>(orders);
    }
  }

  protected abstract List<QuerySortOrder> getDefaultSortOrders();

  private <T extends Serializable> Stream<T> fromPageable(
      PageDTO<T> result, Pageable pageable, Query<T, ?> query) {
    List<T> items = result.getContent();

    int firstRequested = query.getOffset();
    int nrRequested = query.getLimit();
    int firstReturned = pageable.getOffset();
    int firstReal = firstRequested - firstReturned;
    int afterLastReal = firstReal + nrRequested;
    if (afterLastReal > items.size()) {
      afterLastReal = items.size();
    }
    return items.subList(firstReal, afterLastReal).stream();
  }
}
