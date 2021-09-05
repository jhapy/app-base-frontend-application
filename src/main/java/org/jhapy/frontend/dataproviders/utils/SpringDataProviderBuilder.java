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

import com.vaadin.flow.data.provider.*;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-03
 */
public class SpringDataProviderBuilder<T extends Serializable, F> {

  private final BiFunction<Pageable, F, Page<T>> queryFunction;
  private final ToLongFunction<F> lengthFunction;
  private final List<QuerySortOrder> defaultSortOrders = new ArrayList<>();

  private F defaultFilter = null;

  public SpringDataProviderBuilder(
      BiFunction<Pageable, F, Page<T>> queryFunction, ToLongFunction<F> lengthFunction) {
    this.queryFunction = queryFunction;
    this.lengthFunction = lengthFunction;
  }

  public static <T extends Serializable, F> SpringDataProviderBuilder<T, F> forFunctions(
      BiFunction<Pageable, F, Page<T>> queryFunction, ToLongFunction<F> lengthFunction) {
    return new SpringDataProviderBuilder<>(queryFunction, lengthFunction);
  }

  public SpringDataProviderBuilder<T, F> withDefaultSort(String column, SortDirection direction) {
    defaultSortOrders.add(new QuerySortOrder(column, direction));
    return this;
  }

  public SpringDataProviderBuilder<T, F> withDefaultFilter(F defaultFilter) {
    this.defaultFilter = defaultFilter;
    return this;
  }

  public DataProvider<T, F> build() {
    return new PageableDataProvider<>() {
      @Override
      protected Page<T> fetchFromBackEnd(Query<T, F> query, Pageable pageable) {
        return queryFunction.apply(pageable, query.getFilter().orElse(defaultFilter));
      }

      @Override
      protected List<QuerySortOrder> getDefaultSortOrders() {
        return defaultSortOrders;
      }

      @Override
      protected int sizeInBackEnd(Query<T, F> query) {
        return (int) lengthFunction.applyAsLong(query.getFilter().orElse(defaultFilter));
      }
    };
  }

  public ConfigurableFilterDataProvider<T, Void, F> buildFilterable() {
    return build().withConfigurableFilter();
  }
}