# Configuration

There are several ways to configure a Spring application, with the file `src/main/resources/application.properties` commonly used.


Spring supports the `*.properties` file format, but also the YAML format (`*.yml`).

Spring properties can (should) be backed by metadata, which makes it possible to use property values in a [type-safe manner](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-typesafe-configuration-properties) and have IDE support. 


## Usage
To read properties in code, one can use the `@Value` annotation.
As an example, `@Value("my.config.property.key") String value;` can be used to "inject" the value into the field.
Be careful when using this feature, as you should avoid duplicating the dependency to properties throughout your code. Instead, encapsulating the configuration into a service might be the better approach.

Using `@Value` you can also read environment variables, e.g. `@Value("${VCAP_SERVICES}")` (note the `${...}`).

For completeness, you may also inject an `Environment` instance and use it to access the environment.

Properties can also be queried during auto-configuration. For example, the `@ConditionalOnProperty(...)` annotation only considers a configuration class if the required property is set accordingly.

Using the prefix `logging.level` you can configure the log level, for example `logging.level.com.sap.bulletinboard.ads = INFO`.

Netflix Archaius, which is used by Netflix components such as Hystrix, is also connected to the Spring configuration in the sense that Spring properties can be used to configure services querying Archaius properties.
As such, using `hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000` in `application.properties` the default timeout for Hystrix can be set.

## Spring Cloud Config
As modifying a file during runtime is not possible in cloud environments, using this approach it is not possible to use properties to change behavior during runtime.
Furthermore, there may be configuration options that apply to more than a single microservice, where updating the properties in each codebase might be cumbersome and errorprone.

To work around both of these issues, you may use Spring Cloud Config.
With Spring Cloud Config the configuration data is provided by a dedicated server - in our setting a microservice on its own - and updates to this central configuration can be distributed to all "connected" clients automatically, so that the updates become visible and effective during runtime.

To make this idea work, we need to discuss several concepts and adjustments.

### Spring Cloud Config server
The server can be run by simply adding the `@EnableConfigServer` to a Spring configuration class after adding the `spring-cloud-config-server` dependency.
Then, the corresponding application automatically provides configuration using a REST endpoint (serving JSON).
We now just need to provde the desired configuration options to the server, and somehow connect the clients to this server so that updates are detected and processed.

The configuration provided by the server may be split into multiple files.
General configuration can be put into a file named `application.properties`, and its contents are served to all applications.
A file named `application-X.properties` is only served to clients with an active profile `X`, i.e. `application-cloud.properties`.
Values defined in multiple matching property files are overridden by the most concrete file, i.e. values in `application-cloud.properties` take precedence over those in `application.properties`.
Furthermore, application-specific properties may be provided, e.g. `bulletinboard-ads.properties` for microservices with the name `bulletinboard-ads` configured (as described below).
 
The common use case to store the configuration data is to use a Git repository.
With this approach, changes to the configuration data can be traced using the Git history.
Furthermore, issues related to storage and authentication/authorization can be solved using, for example, GitHub.
The Spring Cloud Config server is able to retrieve its data from Git (by specifing the Git URI as `spring.cloud.config.server.git.uri: https://...` in the `application.properties` of the Spring Cloud Config server).
Furthermore, the server is also able to process signals sent by Git servers (for example webhooks sent by GitHub) to automatically forward changes to the clients (the dependency `spring-cloud-config-monitor` needs to be added for this).

In the SAP context, there currently are two issues with storing data in a Git repository.
First, as `github.wdf.sap.corp` is part of the internal network, access to this internal GitHub instance is not possible from Cloud Foundry. Furthermore, when using the public `github.com` (possibly using private repositories) it is currently not possible to send notifications into the server running on Cloud Foundry, as access is only allowed from SAP internal networks. In the long run, these issues might be solved, for example by running Git repositories (GitHub?) in the cloud, or by adjusting the firewall rules accordingly.

As a workaround, in our current experimental server project we store the data as part of the microservice (as files). As such, for every change we need to re-deploy the server. Furthermore, with this approach it is not possible to detect which subset of configuration options changed, as the server has no memory to compare with.
For this, we configure `spring.profiles.active=native` and `spring.cloud.config.server.native.searchLocations=classpath:/clientconfig/`, and put the corresponding files in the `src/main/resources/clientconfig/` directory.
Note that the directories `src/main/resources`  and `src/main/resources/client`directories / and /client are special in the sense that the server silently removes configuration files that match the server's name and profile (e.g. `application.properties`, `application-cloud.properties`).

You can find our example server project [here](https://github.wdf.sap.corp/cc-java-dev/spring-cloud-config-server/).
 
### Spring Cloud Config clients
Assuming that the server is already running in the cloud, we also need to modify the clients to make use of this server.
The dependency `spring-cloud-starter-config` brings in the necessary code, which is auto-configured.

As the configuration should be read from the configuration server, we need to instruct Spring to contact this server on startup.
The file `application.properties` is not suitable for this, as we want its contents to be served by the server.
Instead, we can add a file `bootstrap.properties`, which (as the name implies) is evaluated as one of the fist steps in the Spring start process.
In this file, we add `spring.cloud.config.uri=https://...` to point to the configuration server, and `spring.application.name=...` to configure the client name.

With this configuration, Spring automatically contacts the server, and retrieves the configuration from it before the start process continues.

However, with this the clients are never informed about updates to the configuration.
This can be solved by adding a Spring Cloud Bus dependency (for example `spring-cloud-starter-bus-amqp`) to both the client and the server, and binding the corresponding microservice to the same backing service.
With this, both the client and the server are able to communicate to each other, and notifications for updates on the server are sent to the corresponding clients.  

In order to "see" updated information in Spring, beans using `@Value` can be annotated with `@RefreshScope`.
With this annotation, Spring automatically wraps the bean instance(s) with a proxy, so that updates to the configuration are visible without the need to re-create (and re-inject) the bean instance manually.

