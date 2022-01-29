package org.jhapy.frontend.utils;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.jhapy.dto.utils.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public interface VaadinPageableHelpers extends Serializable {
  static Pageable toPageable(Query<?, ?> vaadinQuery) {
    return Pageable.of(
        vaadinQuery.getPage(),
        vaadinQuery.getPageSize(),
        VaadinPageableHelpers.toPageableSort(vaadinQuery));
  }

  static List<Pageable.Order> toPageableSort(Query<?, ?> vaadinQuery) {
    return vaadinQuery.getSortOrders().stream()
        .map(VaadinPageableHelpers::queryOrderToSpringOrder)
        .collect(Collectors.toList());
  }

  static Pageable.Order queryOrderToSpringOrder(QuerySortOrder queryOrder) {
    return new Pageable.Order(
        queryOrder.getDirection() == SortDirection.ASCENDING
            ? Pageable.Order.Direction.ASC
            : Pageable.Order.Direction.DESC,
        queryOrder.getSorted());
  }
}
