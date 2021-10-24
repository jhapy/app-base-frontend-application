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
import org.jhapy.dto.domain.reference.CountryDTO;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.AppContext;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.reference.ReferenceServices;
import org.jhapy.frontend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class CountryDataProvider extends DefaultDataProvider<CountryDTO, DefaultFilter>
    implements Serializable {

  @Autowired
  public CountryDataProvider() {
    super(
        AppConst.DEFAULT_SORT_DIRECTION,
        new String[] {"name." + AppContext.getInstance().getCurrentIso3Language() + ".value"});
  }

  @Override
  protected PageDTO<CountryDTO> fetchFromBackEnd(
      Query<CountryDTO, DefaultFilter> query, Pageable pageable) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    PageDTO<CountryDTO> page =
        ReferenceServices.getCountryService()
            .findAnyMatching(new FindAnyMatchingQuery(filter.getFilter(), null, pageable))
            .getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }

  @Override
  protected int sizeInBackEnd(Query<CountryDTO, DefaultFilter> query) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    return ReferenceServices.getCountryService()
        .countAnyMatching(new CountAnyMatchingQuery(filter.getFilter(), null))
        .getData()
        .intValue();
  }
}