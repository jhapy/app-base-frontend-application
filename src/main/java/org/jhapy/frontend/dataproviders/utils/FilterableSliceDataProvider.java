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

import com.vaadin.flow.data.provider.Query;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-03
 */
public abstract class FilterableSliceDataProvider<T extends Serializable, F>
    extends SliceDataProvider<T, F> {

  private F filter = null;

  public void setFilter(F filter) {
    if (filter == null) {
      throw new IllegalArgumentException("Filter cannot be null");
    }
    this.filter = filter;
    refreshAll();
  }

  @Override
  public int size(Query<T, F> query) {
    return super.size(getFilterQuery(query));
  }

  @Override
  public Stream<T> fetch(Query<T, F> query) {
    return super.fetch(getFilterQuery(query));
  }

  private Query<T, F> getFilterQuery(Query<T, F> t) {
    return new Query<>(
        t.getOffset(), t.getLimit(), t.getSortOrders(), t.getInMemorySorting(), filter);
  }
}
