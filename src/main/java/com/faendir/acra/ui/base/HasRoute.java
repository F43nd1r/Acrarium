package com.faendir.acra.ui.base;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * @since 18.10.18
 */
public interface HasRoute {
    @NonNull
    Path.Element<?> getPathElement();

    @Nullable
    default Class<? extends HasRoute> getLogicalParent() {
        return null;
    }

    default List<Path.Element<?>> getPathElements(ApplicationContext applicationContext) {
        List<Path.Element<?>> list = new ArrayList<>();
        list.add(getPathElement());
        Class<? extends HasRoute> parent = getLogicalParent();
        if (parent != null) {
            list.addAll(applicationContext.getBean(parent).getPathElements(applicationContext));
        }
        return list;
    }
}
