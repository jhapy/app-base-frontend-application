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

import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Synchronized;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.CountChangedUpdate;
import org.jhapy.cqrs.query.notification.CountAnyMatchingMailTemplateQuery;
import org.jhapy.cqrs.query.notification.CountAnyMatchingMailTemplateResponse;
import org.jhapy.cqrs.query.notification.FindAnyMatchingMailTemplateQuery;
import org.jhapy.cqrs.query.notification.FindAnyMatchingMailTemplateResponse;
import org.jhapy.dto.domain.notification.MailTemplateDTO;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class MailTemplateDataProvider extends JHapyAbstractDataProvider<MailTemplateDTO>
    implements Serializable, HasLogger {

  private transient SubscriptionQueryResult<FindAnyMatchingMailTemplateResponse, MailTemplateDTO>
      fetchQueryResult;

  private transient SubscriptionQueryResult<
          CountAnyMatchingMailTemplateResponse, CountChangedUpdate>
      countQueryResult;

  public MailTemplateDataProvider(QueryGateway queryGateway) {
    super(queryGateway);
  }

  @Override
  @Synchronized
  protected Stream<MailTemplateDTO> fetchFromBackEnd(Query<MailTemplateDTO, Void> query) {
    String loggerPrefix = getLoggerPrefix("fetchFromBackEnd");

    if (fetchQueryResult != null) {
      fetchQueryResult.cancel();
      fetchQueryResult = null;
    }

    FindAnyMatchingMailTemplateQuery fetchCardSummariesQuery =
        new FindAnyMatchingMailTemplateQuery(filter.getFilter(), null, getPageable(query));

    fetchQueryResult =
        queryGateway.subscriptionQuery(
            fetchCardSummariesQuery,
            ResponseTypes.instanceOf(FindAnyMatchingMailTemplateResponse.class),
            ResponseTypes.instanceOf(MailTemplateDTO.class));

    fetchQueryResult
        .updates()
        .subscribe(
            cardSummary -> {
              trace(
                  loggerPrefix,
                  "processing query update for {0}: {1}",
                  fetchCardSummariesQuery,
                  cardSummary);
              fireEvent(new DataChangeEvent.DataRefreshEvent<>(this, cardSummary));
            });

    return fetchQueryResult
        .initialResult()
        .onErrorResume(
            e -> Mono.just(new FindAnyMatchingMailTemplateResponse(Collections.emptyList())))
        .blockOptional()
        .orElse(new FindAnyMatchingMailTemplateResponse(Collections.emptyList()))
        .getData()
        .stream();
  }

  @Override
  @Synchronized
  protected int sizeInBackEnd(Query<MailTemplateDTO, Void> query) {
    String loggerPrefix = getLoggerPrefix("sizeInBackEnd");

    if (countQueryResult != null) {
      countQueryResult.cancel();
      countQueryResult = null;
    }

    CountAnyMatchingMailTemplateQuery countQuery =
        new CountAnyMatchingMailTemplateQuery(filter.getFilter(), null);
    countQueryResult =
        queryGateway.subscriptionQuery(
            countQuery,
            ResponseTypes.instanceOf(CountAnyMatchingMailTemplateResponse.class),
            ResponseTypes.instanceOf(CountChangedUpdate.class));

    countQueryResult
        .updates()
        .buffer(Duration.ofMillis(250))
        .subscribe(
            countChanged -> {
              trace(loggerPrefix, "Processing query update for {0}: {1}", countQuery, countChanged);
              executorService.execute(() -> fireEvent(new DataChangeEvent<>(this)));
            });
    return Math.toIntExact(
        countQueryResult
            .initialResult()
            .onErrorResume(e -> Mono.just(new CountAnyMatchingMailTemplateResponse(0L)))
            .blockOptional()
            .orElse(new CountAnyMatchingMailTemplateResponse(0L))
            .getCount());
  }

  @Synchronized
  void shutDown() {
    if (fetchQueryResult != null) {
      fetchQueryResult.cancel();
      fetchQueryResult = null;
    }
    if (countQueryResult != null) {
      countQueryResult.cancel();
      countQueryResult = null;
    }
  }

  protected List<QuerySortOrder> getDefaultSortOrders() {
    return QuerySortOrder.asc("mailAction").build();
  }
}
