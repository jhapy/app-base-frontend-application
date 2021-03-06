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
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.Serializable;
import org.jhapy.dto.domain.security.SecurityKeycloakGroup;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class SecurityGroupKeycloakDataProvider extends
    DefaultDataProvider<SecurityKeycloakGroup, DefaultFilter> implements
    Serializable {

  @Autowired
  public SecurityGroupKeycloakDataProvider() {
    super(AppConst.DEFAULT_SORT_DIRECTION,
        AppConst.DEFAULT_SORT_FIELDS);
  }

  @Override
  protected Page<SecurityKeycloakGroup> fetchFromBackEnd(
      Query<SecurityKeycloakGroup, DefaultFilter> query,
      Pageable pageable) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    String filterStr =
        filter.getFilter() != null ? filter.getFilter().replaceAll("\\*", "") : null;

    Page<SecurityKeycloakGroup> page = SecurityServices.getKeycloakClient().findGroups(
        new FindAnyMatchingQuery(filterStr, filter.isShowInactive(), pageable)).getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }


  @Override
  protected int sizeInBackEnd(Query<SecurityKeycloakGroup, DefaultFilter> query) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    String filterStr =
        filter.getFilter() != null ? filter.getFilter().replaceAll("\\*", "") : null;

    return SecurityServices.getKeycloakClient()
        .countGroups(new CountAnyMatchingQuery(filterStr, filter.isShowInactive())).getData()
        .intValue();
  }
}
