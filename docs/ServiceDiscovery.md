# Service Discovery
We investigated Eureka as a tool for Service Discovery, and also experimented with Ribbon as a client-side load balancer which uses the data provided by Eureka.
The main benefit of Service Discovery is that, in our setting, microservices are able to find other microservices they depend on, so that it is not necessary to configure these dependencies manually.

In Cloud Foundry it is common to map URLs to applications.
Based on such mappings, incoming requests are redirected to one of the instances of the configured application.
As such, as a developer depending on some kind of service which possibly is scaled to multiple instances (or even running as multiple different applications) it suffices to use the configured URL, and reply on the Cloud Foundry instance handling the redirection.
In case this URL changes, a simple reconfiguration as described [here](Configuration.md) suffices.
In this project, we use this approach for the [UserServiceClient](../src/main/java/com/sap/bulletinboard/ads/services/UserServiceClient.java).

For more advanced usages made possible by Service Discovery (including more refined health checks, client-side load balancing, location awareness) it would be necessary to be able to directly send requests to individual microservice instances.
Providing the corresponding IP address is accomplished using Service Discovery. However, currently the Cloud Foundry network setup disallows direct communication between microservice instances.

Without having direct communication between instances, most interesting use cases of Service Discovery are not possible.
As such, we decided to not include Service Discovery in this project.

[Further reading](https://www.cloudfoundry.org/vision-future-container-networking-cloud-foundry/)