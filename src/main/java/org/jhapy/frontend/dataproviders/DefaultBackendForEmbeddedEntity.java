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

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;
import org.jhapy.dto.domain.BaseEmbeddableEntityDTO;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class DefaultBackendForEmbeddedEntity<C extends BaseEmbeddableEntityDTO>
    extends AbstractBackEndDataProvider<C, CrudFilter> implements Serializable {

  protected final Map<String, C> fieldsMap = new HashMap<>();
  protected final AtomicLong uniqueLong = new AtomicLong();
  private Comparator<C> comparator;
  private SerializablePredicate<C> filter;

  public DefaultBackendForEmbeddedEntity() {}

  public DefaultBackendForEmbeddedEntity(Comparator<C> comparator) {
    this.comparator = comparator;
  }

  @Override
  public abstract Object getId(C value);

  public Map<String, C> getValues() {
    return fieldsMap;
  }

  public abstract void setValues(Map<String, C> values);

  public abstract void persist(C value);

  public abstract void delete(C value);

  public SerializablePredicate<C> getFilter() {
    return this.filter;
  }

  public void setFilter(SerializablePredicate<C> filter) {
    this.filter = filter;
    this.refreshAll();
  }

  public void addFilter(SerializablePredicate<C> filter) {
    Objects.requireNonNull(filter, "Filter cannot be null");
    if (this.getFilter() == null) {
      this.setFilter(filter);
    } else {
      SerializablePredicate<C> oldFilter = this.getFilter();
      this.setFilter((item) -> oldFilter.test(item) && filter.test(item));
    }
  }

  @Override
  protected int sizeInBackEnd(Query<C, CrudFilter> query) {
    return fieldsMap.size();
  }

  @Override
  protected Stream<C> fetchFromBackEnd(Query<C, CrudFilter> query) {
    Stream<C> stream = fieldsMap.values().stream();

    Optional<Comparator<C>> comparing =
        Stream.of(query.getInMemorySorting(), comparator)
            .filter(Objects::nonNull)
            .reduce(Comparator::thenComparing);
    if (comparing.isPresent()) {
      stream = stream.sorted();
    }
    long maxId = 0;
    List<C> result = stream.collect(Collectors.toList());
    for (C c : result) {
      if (c.getId() != null && c.getId() > maxId) {
        maxId = c.getId();
      }
    }
    uniqueLong.set(maxId + 1);
    return result.stream().skip(query.getOffset()).limit(query.getLimit());
  }
}