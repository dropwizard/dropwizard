package com.codahale.dropwizard.views;


/**
 * Provides access to model within the View.
 * <p/>
 * By default, the {@link View} instance itself is passed as a model to a {@link ViewRenderer}.
 * However, if the view class implements this interface the value returned by the
 * {@link #getModel()} method will be used instead.
 *
 * @author Aleksandar Seovic  2013.07.17
 */
public interface ModelContainer<T> {
    /**
     * Returns the model that should be used for view rendering.
     *
     * @return the model that should be used for view rendering
     */
    T getModel();
}
