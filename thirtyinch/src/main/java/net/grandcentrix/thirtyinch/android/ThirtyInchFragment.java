package net.grandcentrix.thirtyinch.android;

import net.grandcentrix.thirtyinch.Presenter;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.View;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class ThirtyInchFragment<V extends View> extends Fragment implements
        View {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + ThirtyInchFragment.class.getSimpleName();

    private volatile boolean mActivityStarted = false;

    private Presenter<V> mPresenter;

    private String mPresenterId;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "onDestroy()");

        if (mPresenter == null) {
            final Bundle activityExtras = activity.getIntent().getExtras();
            mPresenter = providePresenter(activityExtras, getArguments());
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenter.create();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(" + savedInstanceState + ")");
        setRetainInstance(true);

        if (mPresenter == null && savedInstanceState != null) {
            // recover with Savior
            // this should always work.
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);
            if (recoveredPresenterId != null) {
                Log.d(TAG, "try to recover Presenter with id: " + recoveredPresenterId);
                //noinspection unchecked
                mPresenter = PresenterSavior.INSTANCE.recover(recoveredPresenterId);
                if (mPresenter != null) {
                    // save recovered presenter with new id. No other instance of this activity,
                    // holding the presenter before, is now able to remove the reference to
                    // this presenter from the savior
                    PresenterSavior.INSTANCE.free(recoveredPresenterId);
                    mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
                }
                Log.d(TAG, "recovered Presenter " + mPresenter);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        if (isUiPossible()) {
            mPresenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, "onDestroyView()");
        mPresenter.sleep();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach()");
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
        mActivityStarted = true;

        if (isUiPossible()) {
            final V view = provideView();
            mPresenter.bindNewView(view);
            Log.d(TAG, "bound new View (" + view + ") to Presenter (" + mPresenter + ")");
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isUiPossible() && mActivityStarted) {
                        mPresenter.wakeUp();
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()");
        mActivityStarted = false;
        mPresenter.sleep();
        super.onStop();
    }

    @Override
    final public void setRetainInstance(final boolean retain) {
        super.setRetainInstance(true);
    }

    protected Presenter<V> getPresenter() {
        return mPresenter;
    }

    @NonNull
    protected abstract Presenter<V> providePresenter(
            @NonNull final Bundle activityIntentBundle, @NonNull final Bundle fragmentArguments);

    @NonNull
    protected abstract V provideView();

    private boolean isUiPossible() {
        return isAdded() && !isDetached();
    }
}