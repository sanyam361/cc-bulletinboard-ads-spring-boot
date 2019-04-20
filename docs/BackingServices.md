# Backing Services

The backing services bound to an application running in Cloud Foundry are provided as JSON configuration data in the environment variable `VCAP_SERVICES`. Using the Spring Cloud Connector project this information can easily be used that, for example, `DataSource` objects can be configured automatically.
For local execution these beans would have to be configured manually.

By adding the dependency `spring-boot-starter-cloud-connectors` the annotation `@ServiceScan` is made available.
If this annotation is attached to a configuration class that extends `AbstractCloudConfig`, the corresponding beans are registered without the need for more code.
The `@Configuration` class should only be active when the `cloud` profile is active, which is activated by the cloud foundry buildpack. In total, the class looks like this:
```
import org.springframework.cloud.config.java.AbstractCloudConfig;

@Configuration
@ServiceScan
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {

}
```
This suffices to make e.g. the `DataSource` bean available to the JPA setup which depends on it.
