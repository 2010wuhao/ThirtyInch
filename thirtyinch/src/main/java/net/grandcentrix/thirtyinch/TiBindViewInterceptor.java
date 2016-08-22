package net.grandcentrix.thirtyinch;

import net.grandcentrix.thirtyinch.android.callonmainthread.CallOnMainThread;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;

/**
 * Intercepts the supply of the {@link TiView} binding to the {@link TiPresenter}. Allows to proxy
 * the view to add behaviors like {@link DistinctUntilChanged} or {@link
 * CallOnMainThread}
 */
public interface TiBindViewInterceptor {

    <V extends TiView> V intercept(final V view);

}
