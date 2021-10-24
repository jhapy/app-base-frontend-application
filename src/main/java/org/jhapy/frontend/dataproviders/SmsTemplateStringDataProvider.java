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
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.jhapy.dto.domain.notification.SmsTemplate;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.notification.NotificationServices;
import org.jhapy.frontend.dataproviders.utils.PageableDataProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class SmsTemplateStringDataProvider extends PageableDataProvider<SmsTemplate, String>
    implements Serializable {

  @Override
  protected PageDTO<SmsTemplate> fetchFromBackEnd(
      Query<SmsTemplate, String> query, Pageable pageable) {

    return NotificationServices.getSmsTemplateService()
        .findAnyMatching(new FindAnyMatchingQuery(query.getFilter().orElse(null), true, pageable))
        .getData();
  }

  @Override
  protected List<QuerySortOrder> getDefaultSortOrders() {
    return Collections.singletonList(new QuerySortOrder("name", SortDirection.ASCENDING));
  }

  @Override
  protected int sizeInBackEnd(Query<SmsTemplate, String> query) {
    return NotificationServices.getSmsTemplateService()
        .countAnyMatching(new CountAnyMatchingQuery(query.getFilter().orElse(null), true))
        .getData()
        .intValue();
  }
}