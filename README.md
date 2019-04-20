# Overview

This project provides an implementation of the "Bulletinboard Ads" microservice based on [Spring Boot](http://projects.spring.io/spring-boot/).
The microservice is adapted from the code developed in the Cloud Curriculum course [Microservice Development in Java](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/wiki).
The original code can be found [here](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc).

# Table of Contents
This document is divided into the following sections:
 - [Notable Features](#features) - listing the features implemented in this project
 - [Setup and Start](#setupandstart) - a description of how to use this project

In addition, there are several other documents explaining individual parts in more detail:
 - [Spring Boot Basics](docs/SpringBootBasics.md)
 - [Hystrix](docs/Hystrix.md)
 - [Configuration](docs/Configuration.md)
 - [Backing Services](docs/BackingServices.md)
 - [Actuator](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/SpringBoot/docs/Actuator.md)
 - [Logging and Tracing](docs/LoggingTracing.md)
 - [Feature Flags](docs/FeatureFlags.md)
 - [Service Discovery](docs/ServiceDiscovery.md)

# <a name="features"></a>Notable Features
 - REST endpoints using Spring (`@RestController`)
 - Hystrix
 - Tests (Servlet with `RestTemplate`, `MockMvc`, Unit)
 - Security with XS-UAA
 - Logging using SAP CF library (ELK stack compatible)
 - Spring Data JPA (Repositories)
 - Auto-configuration for Cloud Foundry backing services (`@ServiceScan`)
 - support to run on Tomcat (as WAR file)
 - logger "injection" (`@PostProcess` and `BeanPostProcessor`)
 - exposes `/hystrix.stream` for Hystrix Dashboard
 - send and receive messages using RabbitMQ/AMQP
 - exposes a UI5 application via the approuter
 
# <a name="setupandstart"></a>Setup and Start

## Prerequisites
- Have a trial account on [SAP CP Cloud Foundry](https://help.cf.sap.hana.ondemand.com/).
- Setup your development environment with java, maven, node.js, npm, postgresql ... Therefore please follow the [installations steps](https://github.wdf.sap.corp/agile-se/vagrant-development-box/blob/master/VMImage_GettingStarted.md) to prepare your VM image that provides the whole development environment.
- Run `VirtualBox` and start your Virtual Machine (VM).

### Prepare local environment

The `master` branch of the project is setup to use SAP XSUAA.
For this, the local system environment variable `VCAP_SERVICES` must contain the corresponding connection information.
The provided `localEnvironmentSetup.sh` shell script can be used to set the necessary values (via `source localEnvironmentSetup.sh` on a terminal in the project directory). For Eclipse, you need to define the `VCAP_SERVICES` and `VCAP_APPLICATION` environment variable based on this script.

Make sure that PostgreSQL and RabbitMQ are running on the local machine, as referenced in `application.properties`.

Also make sure that you've replaced in your `cc-bulletinboard-ads-spring-boot/` project any occurence of `d012345` or `D012345` by your SAP username.


## Run the application in your local environment
To run the service locally you have two options: Start it directly within Eclipse or via Maven on the command line.

In both cases, your application will be deployed to an embedded Tomcat web server and is visible at the address `http://localhost:8080/api/v1/ads`.

### Run on the command line
Execute in terminal (within project root e.g. ~/git/cc-bulletinboard-ads-spring-boot, which contains the`pom.xml`):
```
source localEnvironmentSetup.sh
mvn spring-boot:run
```

### Run in Eclipse (STS)
In Eclipse Spring Tool Suite (STS) you can import the project as an existing Maven project. There you can start the main method in `com.sap.bulletinboard.ads.BulletinboardAdsApplication`.
You can also right-click on the class in the Package Explorer, and select `Run As` - `Spring Boot App`.

To help during local development, you can change the log format in `src/main/resources/logback.xml` by changing `STDOUT-JSON` to `STDOUT`.

## Test locally
The service endpoints are secured, that means no unauthorized user can access the endpoint. The application expects a so called `JWT` (JSON Web Token) as part of the `Authorization` header of the service that also contains the scope, the user is assigned to.

Test the REST Service `http://localhost:8080/api/v1/ads` manually using the `Postman` chrome extension.

![Post Request using Postman](https://github.com/ccjavadev/cc-bulletinboard-ads-spring-boot/blob/master/CreateMicroservice/images/RestClient_PostRequest.png)

**Note**: For all requests make sure, that you provide a header namely `Authorization` with a JWT token as value e.g. `Bearer eyJhbGciOiJSUzI1NiIs...`. You can generate a valid JWT token as described [in Exercise 24](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/Security/Exercise_24_MakeYourApplicationSecure.md).

- **POST two advertisements**
- As shown in the screenshot above, provide the body type as **raw JSON (application/json)**. This results in the content-type to be set in the header as `Content-Type=application/json`.  

Example Post Body:
```
{
	"title": "hi",
	"price": "50",
	"contact": "myemail",
	"currency": "EUR"
}
```
- Ensure that the location is returned in the header and that the entity is returned in the body.
- **GET all advertisements**  
Ensure that all created advertisements are returned.
- **GET advertisement by id**    
Ensure that the advertisement you created before is returned.


## Steps to deploy to Cloud Foundry

### [Optionally] Build Approuter (a Node.JS application)
Build the Approuter that connects your service to the centrally provided "user account and authentication (UAA) service" which is a JavaScipt application running on NodeJS. With `npm install` the NPM package manager downloads all packages (node modules) it depends on (as defined in [package.json](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/src/main/approuter/package.json)). With this the node modules are downloaded from the SAP internal Nexus registry and are copied into the directory `src/main/approuter/node_modules/approuter`. 

Execute in terminal (within `src/main/approuter` directory, which contains the`package.json`):
```
npm install
```

### Build Advertisement Service (our Java application)
Build the Advertisement Service which is a Java web application running in a Java VM. With `mvn package` Maven build tool takes the compiled code and package it in its distributable format, such as a `JAR` (Java Archive). With this the maven dependencies are downloaded from the SAP internal Nexus registry and are copied into the directory `~/.m2/repository`. Furthermore the JUnit tests (unit tests and component tests) are executed and the `target/bulletinboard-ads.jar` is created. 

Execute in terminal (within root directory, which contains the`pom.xml`):
```
mvn package
```

### Login to Cloud Foundry
Make sure your are logged in to Cloud Foundry and you target your trial space. Run the following commands in the terminal:
```
cf api https://api.cf.sap.hana.ondemand.com
cf login
cf target -o  D012345trial_trial -s dev   ## replace by your space name
```

### Create Services
Create the (backing) services that are specified in the [`manifest.yml`](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/manifest.yml).

Execute in terminal (within root directory, which contains the `security` folder):
```
cf create-service postgresql v9.6-dev postgres-bulletinboard-ads
cf create-service rabbitmq  v3.6-dev mq-bulletinboard
cf create-service xsuaa application uaa-bulletinboard -c security/xs-security.json
cf create-service application-logs lite applogs-bulletinboard
```
> Using the marketplace (`cf m`) you can see the backing services and the plans which are currently available in the cloud.

### Create User-provided Dynatrace Service
https://github.wdf.sap.corp/pages/apm/onboarding/overview.html
- Make sure that Dynatrace is provisioned for your global account and for your user. 
- Generate a PaaS Token and create User-provided Service as described [here:](https://www.dynatrace.com/support/help/infrastructure/paas/how-do-i-monitor-cloud-foundry-applications/)
```
cf cups dynatrace-service -p "environmentid, apitoken, apiurl"
```

> Alternatively you can also remove dynatrace as dependency from your `manifest.yml`.

Further references:
- [APM](https://go.sap.corp/apm)
- [APM Dynatrace Getting Started - SAP Jam](https://jam4.sapjam.com/blogs/show/QkEOymzaJZZbG7r7wEPTTi)
- [Offical Dynatrace documentation](https://www.dynatrace.com/support/help/infrastructure/paas/how-do-i-monitor-cloud-foundry-applications/)
 
### Deploy the approuter and the advertisement service
As a prerequisite step open the `manifest.yml` and replace the d-user by your sap user, to make the routes unique.

The application can be built and pushed using these commands (within root directory, which contains the`manifest.yml`):
```
cf push -f manifest.yml
```
The application will be pushed using the settings in the provided in `manifest.yml`. You can get the exact urls/routes that have been assigned to the application with `cf apps`.

### Create approuter routes per tenant
We make use of the trial subaccount such as `d012345trial` that has a 1-1 relationship to the **Identity Zone `d012345trial`** and which is configured for the **trial CF Org** and is under your control. Note furthermore that the `TENANT_HOST_PATTERN` environment variable ( see `manifest.yml` file) specifies how the approuter should derive the tenant from the URL.
```
cf map-route approuter cfapps.sap.hana.ondemand.com -n d012345trial-approuter-d012345
```

## Test the deployed application 
Open a browser to test whether your microservice runs in the cloud. For this use the approuter URL `https://d012345trial-approuter-d012345.cfapps.sap.hana.ondemand.com/ads/health`. This will bring you the **login page**. Note: You have to enter here your SAP Cloud Identity credentials. After successful login you get redirected to the advertisement service that return you an empty list of advertisements `[]`.

This [`xs-app.json`](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/src/main/approuter/xs-app.json) file specifies how the approuter routes are mapped to the advertisement routes.

Find a step-by-step description on how to test using `Postman` [here](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/Security/Exercise_24_MakeYourApplicationSecure.md).

### UI Testing
Alternatively you can also test your application via the SAP UI5 user interface, which is available under the approuter URL `https://d012345trial-approuter-d012345.cfapps.sap.hana.ondemand.com/`. 

# Continuous Integration -TODO
A **Jenkins Job** is registered as Github Hook and is notfied whenever a new change is pushed to any of the branches. Then the Jenkins Job builds the app, run the tests...

# References
- [Microservice Development in Java - Full Course Outline](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/wiki)
- [CC Exercise 25: System Testing](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/TestStrategy/Exercise_25_Create_SystemTest.md)
- [SAP CP Cockpit](https://account.int.sap.hana.ondemand.com/cockpit#/home/overview)
xx
# cc-bulletinboard-ads-spring-boot
