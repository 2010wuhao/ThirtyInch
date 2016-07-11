package net.grandcentrix.thirtyinch;

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
public class TiMockPresenter extends TiPresenter<TiView> {

    protected int onCreateCalled = 0;

    protected int onDestroyCalled = 0;

    protected int onSleepCalled = 0;

    protected int onWakeUpCalled = 0;

    @Override
    protected void onCreate() {
        super.onCreate();
        onCreateCalled++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyCalled++;
    }

    @Override
    protected void onSleep() {
        super.onSleep();
        onSleepCalled++;
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();
        onWakeUpCalled++;
    }
}
