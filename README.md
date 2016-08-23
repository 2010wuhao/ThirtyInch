# ThirtyInch - a MVP library for Android

> Keep Android At Arm’s Length
>
&mdash; Kevin Schultz, Droidcon NYC '14

According to dangerous experiments, heavy calculations and long running tests the perfect distance to the Android Framework is approximately **thirty inches**.
Sorry for the name, contact us with better ideas.

## Story

Interested in the story and a soft introduction? [Read more here](https://github.com/grandcentrix/thirtyinch/wiki/Introduction)


## Get it

ThirtyInch is available via [jcenter](http://blog.bintray.com/2015/02/09/android-studio-migration-from-maven-central-to-jcenter/)

```gradle
dependencies {
    // MVP for activity and fragment
    compile 'net.grandcentrix.thirtyinch:thirtyinch:0.7.0'
    
    // rx extension
    compile 'net.grandcentrix.thirtyinch:rx:0.7.0'
    
    // test extension
    compile 'net.grandcentrix.thirtyinch:test:0.7.0'
    
    // composite android extension
    compile 'net.grandcentrix.thirtyinch:test:0.7.0'
}
```

## Hello World ThirtyInch

Activity

```java
public class HelloWorldActivity 
        extends TiActivity<HelloWorldPresenter, HelloWorldView> 
        implements HelloWorldView {

    private TextView mOutput;

    @NonNull
    @Override
    public HelloWorldPresenter providePresenter() {
        return new HelloWorldPresenter();
    }

    @Override
    public void showText(final String text) {
        mOutput.setText(text);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mOutput = (TextView) findViewById(R.id.output);
    }
}
```

View

```java
public interface HelloWorldView extends TiView {

    @CallOnMainThread
    void showText(final String text);
}

```

Presenter

```java
public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    @Override
    protected void onWakeUp() {
        super.onWakeUp();
        getView().showText("Hello World!");
    }
}

```

## `ThirtyInch features`

### Presenter

- The `Presenter` survives configuration changes
- The `Presenter` survives when the `Activity` got killed in background
- The `Presenter` is not a singleton
- When the `Activity` gets finished the `Presenter` dies, too

The default behaviour might not fit your needs. 
You can disable unwanted features by providing a configuration in the `TiPresenter` constructor.

```java
public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    public static final TiConfiguration PRESENTER_CONFIG = 
            new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true) 
                .setUseStaticSaviorToRetain(true)
                .setCallOnMainThreadInterceptorEnabled(true)
                .setDistinctUntilChangedInterceptorEnabled(true)
                .build();
            
    public HelloWorldPresenter() {
        super(PRESENTER_CONFIG);
    }
}
```

Or globally for all `TiPresenters`
```java
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        TiPresenter.setDefaultConfig(MY_DEFAULT_CONFIG);
    }
}
```

### View Annotations

Two awesome annotations for the `TiView` interface made it already into `Ti` saving you a lot of time.

```java
public interface HelloWorldView extends TiView {

    @CallOnMainThread
    @DistinctUntilChanged
    void showText(final String text);
}
```

##### @CallOnMainThread

Whenever you call this method it will be called on the Android main thread.
This allows to run code off the main thread but send events to the UI without dealing with Handlers and Loopers.

Requires to be a `void` method. Works only for `TiView` interfaces implemented by "Android Views" (`TiActivity`, `TiFragment`).

Enabled by default, can be disabled with the `TiConfiguration`

##### @DistinctUntilChanged

When calling this method the `View` receives no duplicated calls.
The View swallows the second call when a method gets called with the same (hashcode) parameters twice.

Usecase:
The Presenter binds a huge list to the `View`. The app loses focus (`onSleep()`) and the exact same Activity instance gains focus again (`onWakeUp()`).
The `Activity` still shows the huge list.
The `Presenter` binds the huge list again to the `View`.
When the data has changed the list will be updated.
When the data hasn't changed the call gets swallowed and prevents flickering.

Requires to be a `void` method and has at least one parameter.

Enabled by default, can be disabled with the `TiConfiguration`


### View binding interceptors

*View Annotations* only work because ThirtyInch supports interceptors. 
Add interceptors (`BindViewInterceptor`) to `TiActivity` or `TiFragment` to intercept the binding process from `TiView` to `TiPresenter`.
Interceptors are public API waiting for other great ideas.


```java
public class HelloWorldActivity extends TiActivity<HelloWorldPresenter, HelloWorldView>
        implements HelloWorldView {

    public HelloWorldActivity() {
        addBindViewInterceptor(new MyInterceptor());
    }
}
```


### [Rx](https://github.com/ReactiveX/RxJava)

Using RxJava for networking is very often used.
Observing a `Model` is another good usecase where Rx can be used inside of a `TiPresenter`.
The Rx package provides helper classes to deal with `Subscription` or wait for an attached `TiView`.

```java
public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    // add the subscription helper to your presenter
    private RxTiPresenterSubscriptionHandler rxSubscriptionHelper = new RxTiPresenterSubscriptionHandler(this);

    @Override
    protected void onCreate() {
        super.onCreate();
        
        // automatically unsubscribe in onDestroy()
        rxSubscriptionHelper.manageSubscription(
                Observable.interval(0, 1, TimeUnit.SECONDS)
                    // cache the latest value when no view is attached
                    // emits when the view got attached
                    .compose(RxTiPresenterUtils.<Long>deliverLatestToView(this))
                    .subscribe(uptime -> getView().showPresenterUpTime(uptime))
        );
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();

        // automatically unsubscribe in onSleep()
        rxSubscriptionHelper.manageViewSubscription(anotherObservable.subscribe());

    }
```

### [CompositeAndroid](https://github.com/passsy/CompositeAndroid)

Extending `TiActivity` is probably not what you want because you already have a `BaseActivity`.
Extending all already existing Activities from `TiActivity` doesn't make sense because the don't use MVP right now.
[`CompositeAndroid`](https://github.com/passsy/CompositeAndroid) uses composition to add a `TiPresenter` to an `Activity`.
One line adds the `TiActivityPlugin` and everything works as expected.

```java
public class HelloWorldActivity extends CompositeActivity implements HelloWorldView {

    public HelloWorldActivity() {
        addPlugin(new TiActivityPlugin<HelloWorldPresenter, HelloWorldView>(()-> new HelloWorldPresenter()));
    }
}
```

Yes you have to extends `CompositeActivity`, but that's the last level of inheritance you'll ever need.

## Versions

##### Version 0.7 `26.09.16`
- `TiConfiguration`
- Presenter LifecycleObservers
- ViewBindingInterceptors
- `TiActivityDelegate` for code sharing
- separate Rx module
- separate Test module

##### Version 0.6 `11.06.16`
- Tests
- Smaller bugfixes and minor breaking changes

##### Version 0.5 `03.05.16`
- plugin for CompositeAndroid
- Clean usage syntax by automatically using the `TiActivity` as the `TiView`
- Projects using `Ti`: 5

##### Version 0.4 `12.05.16`
- Extracted into standalone library
- Rebranded to ThirtyInch
- Projects using `Ti`: 3

##### Version 0.3 `19.02.16`
- CallOnMainThread annotation
- fix "Don't keep activities" with `PresenterSavior`

##### Version 0.2 `02.09.15`
- stabilize Activity and Fragment support

##### Version 0.1 `10.04.15`
- first configuration change surviving Presenter
- heavy usage or RxJava


# License

```
Copyright 2016 grandcentrix GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```