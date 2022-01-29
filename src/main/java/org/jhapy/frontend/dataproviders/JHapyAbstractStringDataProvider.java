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

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.utils.Pageable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class JHapyAbstractStringDataProvider<T>
    extends AbstractBackEndDataProvider<T, String> implements Serializable, HasLogger {

  protected static final ExecutorService executorService = Executors.newCachedThreadPool();

  protected final transient QueryGateway queryGateway;

  @Getter
  @Setter
  @NonNull
  @SuppressWarnings("FieldMayBeFinal")
  protected DefaultFilter filter = new DefaultFilter(null);

  protected JHapyAbstractStringDataProvider(QueryGateway queryGateway) {
    this.queryGateway = queryGateway;
  }

  protected static Pageable.Order queryOrderToSpringOrder(QuerySortOrder queryOrder) {
    return new Pageable.Order(
        queryOrder.getDirection() == SortDirection.ASCENDING
            ? Pageable.Order.Direction.ASC
            : Pageable.Order.Direction.DESC,
        queryOrder.getSorted());
  }

  protected Pageable getPageable(Query<T, String> query) {
    Pageable pageable = Pageable.of(query.getPage(), query.getPageSize());
    pageable.setOffset(query.getOffset());
    pageable.setSort(createSpringSort(query));
    return pageable;
  }

  protected <T, String> Collection<Pageable.Order> createSpringSort(Query<T, String> q) {
    List<QuerySortOrder> sortOrders;
    if (q.getSortOrders() == null || q.getSortOrders().isEmpty()) {
      sortOrders = getDefaultSortOrders();
    } else {
      sortOrders = q.getSortOrders();
    }
    List<Pageable.Order> orders =
        sortOrders.stream().map(JHapyAbstractStringDataProvider::queryOrderToSpringOrder).toList();
    if (orders.isEmpty()) {
      return Collections.emptyList();
    } else {
      return new ArrayList<>(orders);
    }
  }

  protected List<QuerySortOrder> getDefaultSortOrders() {
    return QuerySortOrder.asc("name").build();
  }
}
