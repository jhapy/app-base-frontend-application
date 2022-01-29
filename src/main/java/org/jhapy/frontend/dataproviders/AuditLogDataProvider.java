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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jhapy.dto.domain.audit.AuditLog;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.auditLog.CountAuditLogQuery;
import org.jhapy.dto.serviceQuery.auditLog.FindAuditLogQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.dataproviders.AuditLogDataProvider.AuditLogFilter;
import org.jhapy.frontend.utils.AppConst;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class AuditLogDataProvider extends DefaultDataProvider<AuditLog, AuditLogFilter>
    implements Serializable {

  private final Function<FindAuditLogQuery, ServiceResult<PageDTO<AuditLog>>> findHandler;
  private final Function<CountAuditLogQuery, ServiceResult<Long>> countHandler;

  public AuditLogDataProvider(
      Function<FindAuditLogQuery, ServiceResult<PageDTO<AuditLog>>> findHandler,
      Function<CountAuditLogQuery, ServiceResult<Long>> countHandler) {
    super(AppConst.DEFAULT_SORT_DIRECTION, AppConst.DEFAULT_SORT_FIELDS);
    this.findHandler = findHandler;
    this.countHandler = countHandler;
  }

  @Override
  protected PageDTO<AuditLog> fetchFromBackEnd(
      Query<AuditLog, AuditLogFilter> query, Pageable pageable) {
    AuditLogFilter filter = query.getFilter().orElse(null);
    PageDTO<AuditLog> page =
        findHandler
            .apply(new FindAuditLogQuery(filter.getClassName(), filter.getRecordId(), pageable))
            .getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }

  @Override
  protected int sizeInBackEnd(Query<AuditLog, AuditLogFilter> query) {
    AuditLogFilter filter = query.getFilter().orElse(null);

    ServiceResult<Long> _count =
        countHandler.apply(new CountAuditLogQuery(filter.getClassName(), filter.getRecordId()));

    if (_count.getIsSuccess() && _count.getData() != null) {
      return _count.getData().intValue();
    } else {
      return 0;
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @AllArgsConstructor
  public static class AuditLogFilter extends DefaultFilter {

    private String className = null;
    private UUID recordId = null;
  }
}
