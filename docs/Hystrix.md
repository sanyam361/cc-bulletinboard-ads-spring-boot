# Hystrix
Hystrix may be added using `spring-cloud-starter-netflix-hystrix` and works just as expected.

## JavaNica
Additionally, JavaNica is included as a dependency, which makes it possible to add annotations to methods so that invoking these internally uses Hystrix.
However, for non-synchronous calls, the code in these methods still needs to provide the Future or Observable results, which does not give a big advantage (see below).
As such, the current approach without JavaNica seems to be good enough.

```
@HystrixCommand
public Observable<User> getUserById(final String id) {
  return Observable.create(new Observable.OnSubscribe<User>() {
    @Override
    public void call(Subscriber<? super User> observer) {
      try {
        if (!observer.isUnsubscribed()) {
          observer.onNext(new User(id, name + id));
          observer.onCompleted();
        }
      } catch (Exception e) {
        observer.onError(e);
      }
    }
  });
}
```

## Dashboard
The stream which may be consumed by the Hystrix Dashboard is exposed using the [HystrixConfig.java](../src/main/java/com/sap/bulletinboard/ads/config/HystrixConfig.java) configuration.
To consume and visualize this data, the Hystrix Dashboard may be used by providing an (otherwise empty) Spring Boot application with the `spring-cloud-starter-netflix-hystrix-dashboard` dependency and `@EnableHystrixDashboard` on a configuration class.
