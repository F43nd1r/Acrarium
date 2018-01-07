package com.faendir.acra.dataprovider;

import com.vaadin.data.provider.DataProvider;

/**
 * @author Lukas
 * @since 06.01.2018
 */
public interface ObservableDataProvider<T,F> extends DataProvider<T,F> {
    void addSizeListener(SizeListener listener);

    void removeSizeListener(SizeListener listener);

    interface SizeListener {
        void sizeChanged(int size);
    }
}
