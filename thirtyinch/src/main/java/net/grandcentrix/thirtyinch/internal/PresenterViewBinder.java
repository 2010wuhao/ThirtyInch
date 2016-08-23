package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Binds a {@link TiView} to a {@link TiPresenter} and allows {@link BindViewInterceptor}s to
 * pivot the view before attaching
 *
 * @param <V> the {@link TiView}
 */
public class PresenterViewBinder<V extends TiView> implements InterceptableViewBinder<V> {

    private List<BindViewInterceptor> mBindViewInterceptors = new ArrayList<>();

    private HashMap<BindViewInterceptor, V> mIntercepterViewOutput = new HashMap<>();

    /**
     * the cached version of the view send to the presenter after it passed the interceptors
     */
    private V mLastView;

    private final TiPresenterLogger mLogger;

    public PresenterViewBinder(final TiPresenterLogger logger) {
        mLogger = logger;
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(final BindViewInterceptor interceptor) {
        mBindViewInterceptors.add(interceptor);
        invalidateView();

        return new OnTimeRemovable() {
            @Override
            public void onRemove() {
                mBindViewInterceptors.remove(interceptor);
                invalidateView();
            }
        };
    }

    /**
     * binds the view (this Activity) to the {@code presenter}. Allows interceptors to change,
     * delegate or wrap the view before it gets attached to the presenter.
     */
    public void bindView(final TiPresenter<V> presenter, final TiViewProvider<V> viewProvider) {
        if (mLastView == null) {
            invalidateView();
            V interceptedView = viewProvider.provideView();
            for (final BindViewInterceptor interceptor : mBindViewInterceptors) {
                interceptedView = interceptor.intercept(interceptedView);
                mIntercepterViewOutput.put(interceptor, interceptedView);
            }
            mLastView = interceptedView;
            mLogger.logTiMessages("binding NEW view to Presenter " + mLastView);
            presenter.bindNewView(mLastView);
        } else {
            mLogger.logTiMessages("binding the cached view to Presenter " + mLastView);
            presenter.bindNewView(mLastView);
        }
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(final BindViewInterceptor interceptor) {
        return mIntercepterViewOutput.get(interceptor);
    }

    @NonNull
    @Override
    public List<BindViewInterceptor> getInterceptors(
            final Filter<BindViewInterceptor> predicate) {
        final ArrayList<BindViewInterceptor> result = new ArrayList<>();
        for (int i = 0; i < mBindViewInterceptors.size(); i++) {
            final BindViewInterceptor interceptor = mBindViewInterceptors.get(i);
            if (predicate.apply(interceptor)) {
                result.add(interceptor);
            }
        }
        return result;
    }

    @Override
    public void invalidateView() {
        mLastView = null;
        mIntercepterViewOutput.clear();
    }
}
