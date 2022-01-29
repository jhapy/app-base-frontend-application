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
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;

import java.io.Serializable;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class DefaultSearchDataProvider<T extends SearchQueryResult, F extends SearchQuery>
    extends AbstractBackEndDataProvider<T, F> implements Serializable {

  public DefaultSearchDataProvider() {}

  @Override
  public Object getId(T item) {
    return item.getId();
  }

  public static class DefaultFilter implements Serializable {

    private String filter;
    private Boolean showInactive;

    public DefaultFilter() {}

    public DefaultFilter(String filter) {
      this(filter, null);
    }

    public DefaultFilter(String filter, Boolean showInactive) {
      this.filter = filter;
      this.showInactive = showInactive;
    }

    public static DefaultFilter getEmptyFilter() {
      return new DefaultFilter(null, null);
    }

    public String getFilter() {
      return filter;
    }

    public void setFilter(String filter) {
      this.filter = filter;
    }

    public Boolean isShowInactive() {
      return showInactive;
    }

    public void setShowInactive(Boolean showInactive) {
      this.showInactive = showInactive;
    }
  }
}
