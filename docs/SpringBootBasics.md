# Spring Boot Basics
The entry point of a Spring Boot application is a class annotated with `@SpringBootApplication`, in our case `BulletinboardAdsApplication`.
Using the provided `main` method Spring is instructed to create a new application context and configure itself as described in the referenced class.
In this case the configuration details are provided implicitly using the `@SpringBootApplication` annotation, as explained below.

## Introduction of Spring Dependency Injection Framework
Please have a look at the [Spring DI Basics Introduction](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/SpringBasics/Readme.md).

## `@SpringBootApplication`
The annotation `@SpringBootAnnotation` is a meta-annotation combining several other annotations.
For example, `@SpringBootApplication` includes `@ComponentScan` (the effects of which are explained below), so adding `@SpringBootApplication` to the class `BulletinboardAdsApplication` automatically enables `@ComponentScan`.


## `@SpringBootConfiguration`
The annotation `@SpringBootConfiguration`, which is also part of `@SpringBootApplication`, includes the annotation `@Configuration`.
The main effect is that you may add bean definitions in the style of `@Bean X x() { return new X(); }` to any class annotated with `@SpringBootApplication` (and of course also to other classes annotated with `@Configuration`).
 

## `@EnableAutoConfiguration`
Furthermore, `@SpringBootApplication` is annotated with `@EnableAutoConfiguration`.
Because of this, several other beans are automatically registered and configured.
As a simple example, if the necessary (Maven) dependencies are provided, Spring automatically creates a `RabbitTemplate` bean which connects to a `RabbitMQ` server running on `localhost`.

The auto-configuration as done by Spring Boot is often described as having an "opinionated view on Spring", meaning that reasonable defaults are chosen despite having many other possibilities in Spring.
In the example of `RabbitTemplate`, the default is to configure and create the bean so that it connects to `localhost`, but you can also disable this behaviour, or reconfigure the bean so that it connects to (for example) a backing service in Cloud Foundry.

The main advantage of Spring Boot and its auto-configuration is that several components easily integrate with each other, for example endpoints which are automatically offered using Spring MVC, or communication classes which use Eureka+Hystrix+Ribbon+Sleuth with only little manual configuration or code changes.
Together with the fact that the versions of several components are set in a central place (to avoid dependency issues), this can help developing faster.

The auto-configuration internally uses annotations like `@ConditionalOnMissingBean` and `@ConditionalOnProperty`.
As an example, the `RabbitTemplate` bean in `RabbitAutoConfiguration` is only created if no `RabbitTemplate` bean was defined elsewhere, as indicated by `@ConditionalOnMissingBean(RabbitTemplate.class)`.

Auto-configuration classes may be disabled using Spring properties, e.g by adding `spring.autoconfigure.exclude = org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration` to `src/main/resources/application.properties`.


## Configuring Spring Boot
The steps necessary to define a Spring Boot application with component scan and auto-configuration, but without any useful features, is shown in [this commit](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-boot/commit/5dc7a232d8a3e4cd4959d50cda8c16d7b4e97b47). 

As seen in the `pom.xml` file, Spring (Boot) components are added to the project by defining `spring-boot-starter` dependencies.
For example, the web server functionality is added using `spring-boot-starter-web`.
The internal dependencies (Tomcat, Hibernate Validator, ...) are automatically added, and their versions are managed by `spring-boot-starter-parent`.

For Spring Cloud components a dedicated dependency management block needs to be added, as shown in [this commit](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-boot/commit/3cacd0575d219807de50250782a14d958396717f) when added Spring Cloud Connectors.
 
## Packaging (JAR, WAR) 
Using the `spring-boot-maven-plugin`, Spring Boot packages the application into a JAR or WAR file (depending on the `packaging` option set in `pom.xml`).
The JAR file uses the `main` method, as explained above.
When pushing a WAR file created by Spring Boot to Cloud Foundry, the buildpack (strangely?) also uses the `main` method to start the application (potentially only if Tomcat is detected as part of the WAR file).
Using a [ServletInitializer](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-boot/blob/master/src/main/java/com/sap/bulletinboard/ads/ServletInitializer.java) it is also possible to deploy the application on an existing application server (e.g. Tomcat).

Note that the JAR file created by Spring Boot [does not "explode" the JAR files of the dependencies](http://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html).
Instead, using a dedicated classloader, the JAR file contains nested JAR files.
This has some advantages, but also means that (currently) Maven Failsafe is not able to work with such JAR files.
A workaround is implemented and documented in the `pom.xml` file.

The created JAR/WAR file starts the main method in `JarLauncher`/`WarLauncher`, which is provided by Spring Boot and internally starts the application using the mentioned classloader.

# Code example, Testing
In [this commit](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-boot/commit/05a35e1dfc359f127d1af6ac0da0237d3ef07e60) you can see a simple extension of the otherwise empty project, so that a REST endpoint is offered using Spring MVC.
By adding the `spring-boot-starter-web` dependency, Spring Boot automatically configures an embedded Tomcat server to run on port 8080, and configures Spring MVC to make use of it.
The REST controller in the class `ExampleController` is found and registered automatically, as the `@RestController` annotation is annotated with `@Component`.

Using `@RestController` on the class and `@GetMapping` on the method, Spring automatically routes HTTP requests to `http://localhost:8080/` to this method, so that "OK" appears in the browser.

The annotation `@GetMapping` is a shortcut for `@RequestMapping(method = GET)`, which was introduced in Spring Boot 1.4 (along with `@PutMapping` etc.).

In the `AdvertisementControllerTest` class you see how a controller can be tested.
Using `@RunWith(SpringRunner.class)` JUnit is instructed to pass control over to Spring, so that Spring features can be used in the test. The class is annotated with `@SpringBootTest` so the entire application context is created for the test.
There are also other annotations like `@WebMvcTest` which can be used instead of `@SpringBootTest` so only a subset of the context is created, which can speed up the test setup.

## Further References
- [Spring DI Basics Introduction](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/SpringBasics/Readme.md)
- [Spring tutorial - Wiki](https://github.wdf.sap.corp/d022051/SpringTutorial/wiki)
