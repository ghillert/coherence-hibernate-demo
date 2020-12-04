# Coherence Hibernate Demo

![Java CI with Maven](https://github.com/ghillert/coherence-hibernate-demo/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)
[![License](http://img.shields.io/badge/license-UPL%201.0-blue.svg)](https://oss.oracle.com/licenses/upl/)
[![Chat with us on slack](https://img.shields.io/badge/Coherence-Join%20Slack-red)](https://join.slack.com/t/oraclecoherence/shared_invite/zt-9ufv220y-Leudk0o5ntgNV0xraa8DNw)
[![Twitter Follow](https://img.shields.io/twitter/follow/OracleCoherence?style=social)](https://twitter.com/OracleCoherence)


## Overview

In this demo we are adding Hibernate Second Level Caching to a simple Spring Boot
application using the [Coherence Hibernate project](https://github.com/coherence-community/coherence-hibernate). As part of the demo, we can create and query for `Events`. Furthermore, we can
add `People` and each `Person` can be added to an event.

The demo is split into multiple Maven modules in order to show-case 2 use-cases:

- Embedded Coherence
- Use a remote Coherence cache using [Coherence*Extend](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.0/develop-remote-clients/introduction-coherenceextend.html)

The Maven Project is structured as follows:

- **coherence-hibernate-demo-app** Main entry point for the demo
- **coherence-hibernate-demo-server** Remote Coherence server
- **coherence-hibernate-demo-core** Used to share serialization bits between local app and remote Coherence server

## How to Run

Check out the project using [Git](https://git-scm.com/):

```bash
git clone https://github.com/ghillert/coherence-hibernate-demo.git
cd coherence-hibernate-demo
```

Build the demo using [Maven](https://maven.apache.org/):

```bash
./mvnw clean package
```

## Run the embedded Coherence demo

Run the demo:

```bash
java -jar coherence-hibernate-demo-app/target/coherence-hibernate-demo-app-1.0.0-SNAPSHOT.jar
```

## Run the remote Coherence demo

First, start the remote Coherence server:

```bash
java -jar coherence-hibernate-demo-server/target/coherence-hibernate-demo-server-1.0.0-SNAPSHOT.jar
```

Next, start the demo:

```bash
java -jar coherence-hibernate-demo-app/target/coherence-hibernate-demo-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=remote
```

We will use the exact same application as used for the embedded Coherence demo. However by activating the `remote` Spring Boot profile using `--spring.profiles.active=remote`, we use a different chache config file `remote-hibernate-second-level-cache-config.xml` defined in `application-remote.yml`.

## Execute the REST enpoints

Once started the embedded database is empty. Let's create an event and 2 people:

```bash
curl --request POST 'http://localhost:8080/api/events?title=First%20Event&date=2020-11-30'
curl --request POST 'http://localhost:8080/api/people?firstName=Conrad&lastName=Zuse&age=85'
curl --request POST 'http://localhost:8080/api/people?firstName=Alan&lastName=Turing&age=41'
curl --request POST 'http://localhost:8080/api/people/2/add-to-event/1'
curl --request POST 'http://localhost:8080/api/people/3/add-to-event/1'
```

If you query for the event at `http://localhost:8080/api/events/1` you will be able to retrieve your event. For the
first time you will see an SQL query being executed but subsequent queries will retrieve
the result from the second level cache.

In the console you should some basic statistics being printed:

```
2020-11-02 14:39:31.060  INFO 2685 --- [nio-8080-exec-2] i.StatisticalLoggingSessionEventListener : Session Metrics {
    19558 nanoseconds spent acquiring 1 JDBC connections;
    0 nanoseconds spent releasing 0 JDBC connections;
    0 nanoseconds spent preparing 0 JDBC statements;
    0 nanoseconds spent executing 0 JDBC statements;
    0 nanoseconds spent executing 0 JDBC batches;
    0 nanoseconds spent performing 0 L2C puts;
    1857373 nanoseconds spent performing 1 L2C hits;
    0 nanoseconds spent performing 0 L2C misses;
    163367 nanoseconds spent executing 1 flushes (flushing a total of 1 entities and 1 collections);
    0 nanoseconds spent executing 0 partial-flushes (flushing a total of 0 entities and 0 collections)
```

You can also see statistics at `http://localhost:8080/api/statistics`.

## More Details

The Spring Boot application was created via https://start.spring.io. This is generally a good starting if you new to Spring Boot:

```bash
wget https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.4.0.M4&packaging=jar&jvmVersion=11&groupId=com.oracle.coherence.hibernate&artifactId=spring-demo&name=spring-demo&description=Demo%20project%20for%20Coherence%20Hibernate&packageName=com.oracle.coherence.hibernate.demo&dependencies=data-jpa,web,hsql
```

We have added 2 domain objects to the application:

* Person
* Event

## Enable Caching using Coherence CE

### Add Required Dependencies

First, please add the respective dependency to your `pom.xml`. In our case, we are using Hibernate version `5.2.17.Final`. Therefore, the dependency to add is:

```xml
<dependency>
    <groupId>com.oracle.coherence.hibernate</groupId>
    <artifactId>coherence-hibernate-cache-52</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

You will also need to add a specific version of Coherence, e.g.:

```xml
<dependency>
    <groupId>com.oracle.coherence.ce</groupId>
    <artifactId>coherence</artifactId>
    <version>20.06.1</version>
</dependency>
```

### Configure Coherence

The Coherence caches and mappings are defined in

- `hibernate-second-level-cache-config.xml` (Embedded Coherence)
- `remote-hibernate-second-level-cache-config.xml` (Remote Coherence)

### Configure Hibernate

In order to configure Hibernate, we will set the following Hibernate properties:

```properties
hibernate.cache.use_second_level_cache=true
hibernate.cache.region.factory_class=com.oracle.coherence.hibernate.cache.CoherenceRegionFactory
hibernate.cache.use_query_cache=true
```
For our Spring Boot based application, we can set those properties in
`application.yml`:

```yaml
spring:
  jpa:
    hibernate:
        properties:
          hibernate.cache.use_second_level_cache: true
          hibernate.cache.region.factory_class: com.oracle.coherence.hibernate.cache.CoherenceRegionFactory
          hibernate.cache.use_query_cache: true
          com.oracle.coherence.hibernate.cache.cache_config_file_path: test-hibernate-second-level-cache-config.xml
```

In order to support the remote Coherence cache server use-case, we have also added a profile-specific yaml file that only is active when the `remote` profile is activated - `application-Remote.yml`:

```yaml
spring:
  jpa:
    properties:
      com.oracle.coherence.hibernate.cache.cache_config_file_path: remote-hibernate-second-level-cache-config.xml
```

### Configure Domain Classes

By default, your entity/model/domain objects are not cached. In order to make them cacheable, we will use the `@Cache` annotation:

```java
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
```

Furthermore, we have 3 REST controllers.

## EventController

- GET `/api/events` Gets a paginated list of events
- POST `/api/events?title=foo&data=2020-10-30` Create a single event

## PersonController

- GET `/api/people` Gets a paginated list of events
- POST `/api/people?firstname=Eric&lastname=Cartman&age=10` Create a single person
- POST `/api/people/{personId}/add-to-event/{eventId}` Add a person to an event

## StatisticsController

- GET `/api/statistics` Return Hibernate statistics

With caching you may see the following response:

```json
{
  "startTime" : 1604093369323,
  "sessionOpenCount" : 6,
  "sessionCloseCount" : 5,
  "flushCount" : 0,
  "connectCount" : 1,
  "prepareStatementCount" : 0,
  "closeStatementCount" : 0,
  "entityLoadCount" : 0,
  "entityUpdateCount" : 0,
  "entityInsertCount" : 0,
  "entityDeleteCount" : 0,
  "entityFetchCount" : 0,
  "collectionLoadCount" : 0,
  "collectionUpdateCount" : 0,
  "collectionRemoveCount" : 0,
  "collectionRecreateCount" : 0,
  "collectionFetchCount" : 0,
  "secondLevelCacheHitCount" : 0,
  "secondLevelCacheMissCount" : 0,
  "secondLevelCachePutCount" : 0,
  "naturalIdCacheHitCount" : 0,
  "naturalIdCacheMissCount" : 0,
  "naturalIdCachePutCount" : 0,
  "naturalIdQueryExecutionCount" : 0,
  "naturalIdQueryExecutionMaxTime" : 0,
  "naturalIdQueryExecutionMaxTimeRegion" : null,
  "queryExecutionCount" : 0,
  "queryExecutionMaxTime" : 0,
  "queryExecutionMaxTimeQueryString" : null,
  "queryCacheHitCount" : 0,
  "queryCacheMissCount" : 0,
  "queryCachePutCount" : 0,
  "updateTimestampsCacheHitCount" : 0,
  "updateTimestampsCacheMissCount" : 0,
  "updateTimestampsCachePutCount" : 0,
  "transactionCount" : 1,
  "optimisticFailureCount" : 0,
  "statisticsEnabled" : true,
  "queries" : [ ],
  "entityNames" : [ "com.oracle.coherence.hibernate.demo.model.Person", "com.oracle.coherence.hibernate.demo.model.Event" ],
  "collectionRoleNames" : [ "com.oracle.coherence.hibernate.demo.model.Person", "com.oracle.coherence.hibernate.demo.model.Event" ],
  "secondLevelCacheRegionNames" : [ ],
  "successfulTransactionCount" : 1
}
```
