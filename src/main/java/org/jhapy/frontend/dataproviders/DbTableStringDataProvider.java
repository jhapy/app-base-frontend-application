/*
 *
 *  DO YOU WANNA ENJOY CONFIDENTIAL
 *   __________________
 *
 *   [2018] - [2019] Do You Wanna Play
 *   All Rights Reserved.
 *
 *   NOTICE:  All information contained herein is, and remains the property of "Do You Wanna Play"
 *   and its suppliers, if any. The intellectual and technical concepts contained herein are
 *   proprietary to "Do You Wanna Play" and its suppliers and may be covered by Morocco. and Foreign
 *   Patents, patents in process, and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material is strictly forbidden unless
 *    prior written permission is obtained from "Do You Wanna Play".
 */

package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.jhapy.dto.domain.DbTableDTO;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.dataproviders.utils.PageableDataProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class DbTableStringDataProvider extends PageableDataProvider<DbTableDTO, String>
    implements Serializable {

  @Override
  protected PageDTO<DbTableDTO> fetchFromBackEnd(
      Query<DbTableDTO, String> query, Pageable pageable) {
    PageDTO<DbTableDTO> page =
        SecurityServices.getTableService()
            .findAnyMatching(
                new FindAnyMatchingQuery(query.getFilter().orElse(null), false, pageable))
            .getData();
    return page;
  }

  @Override
  protected List<QuerySortOrder> getDefaultSortOrders() {
    return Collections.singletonList(new QuerySortOrder("name", SortDirection.ASCENDING));
  }

  @Override
  protected int sizeInBackEnd(Query<DbTableDTO, String> query) {
    return SecurityServices.getTableService()
        .countAnyMatching(new CountAnyMatchingQuery(query.getFilter().orElse(null), false))
        .getData()
        .intValue();
  }
}
