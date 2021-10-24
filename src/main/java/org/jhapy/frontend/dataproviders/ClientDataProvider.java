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
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.jhapy.dto.domain.ClientDTO;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.utils.AppConst;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class ClientDataProvider extends DefaultDataProvider<ClientDTO, DefaultFilter>
    implements Serializable {

  @Autowired
  public ClientDataProvider() {
    super(AppConst.DEFAULT_SORT_DIRECTION, AppConst.DEFAULT_SORT_FIELDS);
  }

  @Override
  protected PageDTO<ClientDTO> fetchFromBackEnd(
      Query<ClientDTO, DefaultFilter> query, Pageable pageable) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    PageDTO<ClientDTO> page =
        SecurityServices.getClientService()
            .findAnyMatching(
                new FindAnyMatchingQuery(filter.getFilter(), filter.isShowInactive(), pageable))
            .getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }

  @Override
  protected int sizeInBackEnd(Query<ClientDTO, DefaultFilter> query) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    return SecurityServices.getClientService()
        .countAnyMatching(new CountAnyMatchingQuery(filter.getFilter(), filter.isShowInactive()))
        .getData()
        .intValue();
  }
}