package com.faendir.acra.util;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.springframework.data.domain.Sort;

/**
 * @author Lukas
 * @since 13.12.2017
 */
public class OrderAdapter extends Sort.Order {
    public OrderAdapter(QuerySortOrder adapt) {
        super(adapt.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC, adapt.getSorted());
    }
}
