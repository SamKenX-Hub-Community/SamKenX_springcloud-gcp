## Migration Guide from Spring Cloud GCP 3.x to 4.x

### Before you start

#### Upgrade to the 3.x version
This doc assumes you are running with Spring Cloud GCP 3.x.

If you are currently running with an earlier major version of Spring Cloud GCP, i.e., 1.x or 2.x, we recommend that you upgrade to [Spring Cloud GCP 3.x](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/releases/tag/v3.0.0) before migrating to Spring Cloud GCP 4.0.

- [Migration guide from Spring Cloud GCP 1.x to 2.x](./migration-guide-1.x.md)
- [Spring Cloud GCP 3.0 Release Note](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/releases/tag/v3.0.0)

Note that since Spring Cloud GCP 3.0 has few breaking changes, we only provide a release note.

#### Review System requirements
Spring Cloud GCP 4.0 is built on Spring Boot 3.0 and Spring Framework 6.0, which requires Java 17 at minimum.
If you are currently on Java 8 or Java 11, you need to upgrade your JDK before you can develop an application based on Spring Cloud GCP 4.0.

#### Review Deprecations from 3.x
Classes, methods and properties that were deprecated in Spring Cloud GCP 3.x have been removed in this release.
Please ensure that you aren’t calling deprecated methods before upgrading.

### Upgrade to Spring Cloud GCP 4.0
#### Spring Boot 3.0
Spring Cloud GCP 4.0 builds on Spring Boot 3.0. Review the [Spring Boot 3.0 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide) before continuing.

#### Update Bill of Materials (BOM)
If you’re a Maven user, add our BOM to your pom.xml `<dependencyManagement>` section.
This will allow you to not specify versions for any of the Maven dependencies and instead delegate versioning to the BOM.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.google.cloud</groupId>
            <artifactId>spring-cloud-gcp-dependencies</artifactId>
            <version>4.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

##### Review Dependencies
Run `mvn dependency:tree` (`gradlew dependencies` for Gradle projects) to see the dependency tree of your project. 
Ensure Spring-related dependencies have matching versions:

- Spring Boot 3.0
- Spring Cloud 2022.0
  - For detailed dependency versions, see "2022.0 (Kilburn)" column in [Spring Cloud: Supported Versions](https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions#supported-releases) table.
- Spring Data 2022.0
  - For detailed dependency versions, see [Spring Data 2022.0 Release Note](https://github.com/spring-projects/spring-data-commons/wiki/Spring-Data-2022.0-%28Turing%29-Release-Notes).

#### Cloud BigQuery
Replaced `ListenableFuture` and `SettableListenableFuture` with `CompletableFuture` as the former two Classes are deprecated in Spring Framework 6.0.

#### Cloud SQL
MySQL R2DBC is not supported by Spring Cloud GCP 4.0 due to [compatibility issues](https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory/issues/990) with the Mariadb driver.

#### Cloud Pub/Sub
Replaced `ListenableFuture` and `SettableListenableFuture` with `CompletableFuture` as the former two Classes are deprecated in Spring Framework 6.0.

#### Spring Cloud Stream binder to Google Cloud Pub/Sub
Change to functional model as annotation-based model is removed from Spring Cloud Stream 4.0.0.

#### Secret Manager
To enable the application to fetch a secret from SecretManager, add `spring.config.import=sm://` in the `application.properties` or `application.yml` files and inject the secret into the application by adding `application.secret=${sm://your-secret-name}`.
Do not write secret values in the file.

Please also remove `spring.cloud.gcp.secretmanager.enabled` and `spring.cloud.gcp.secretmanager.legacy` in `bootstrap.yml` or `bootstrap.properties` files.

#### Cloud Trace
Annotation `org.springframework.cloud.sleuth.annotation.NewSpan` is deprecated in favor of `io.micrometer.observation.annotation.Observed` since Spring Boot 3.0 builds on Micrometer 1.0 rather than Spring Cloud Sleuth (which is excluded from the Spring Cloud release train).

If you use `NewSpan` to generate an additional span, replace the annotation with `Observed`.
The default name of the additional span is `<class-name>#<method-name>`.
If you want to customize the additional span name, specify the `contextualName` parameter.

Please also remove `spring.sleuth.enabled` from your `application.properties` or `application.yml` file.
Note that `spring.cloud.gcp.trace.enabled=true` is still required if you want to enable Trace.

#### Cloud Vision
Replaced `ListenableFuture` and `SettableListenableFuture` with `CompletableFuture` as the former two Classes are deprecated in Spring Framework 6.0.

### Configuration Changelog
#### Removed
The following properties are removed from the codebase thus no longer needed in your application.

- `spring.cloud.gcp.secretmanager.enabled`

- `spring.cloud.gcp.secretmanager.legacy`

See the "SecretManager" section.

- `spring.sleuth.enabled`

See the "Trace" section

### Deprecated Items Removed

#### Cloud BigQuery
- `BigQueryTemplate(BigQuery bigQuery, String datasetName)`
  Use `BigQueryTemplate(BigQuery, BigQueryWriteClient, Map, TaskScheduler)` instead

- `BigQueryTemplate(BigQuery, String, TaskScheduler)`
  Use `BigQueryTemplate(BigQuery, BigQueryWriteClient, Map, TaskScheduler)` instead

#### Cloud Datastore
- `DatastorePersistentPropertyImpl.getPersistentEntityTypes()`
  Use `DatastorePersistentPropertyImpl.getPersistentEntityTypeInformation()` instead

#### Cloud Pub/Sub
- `DefaultSubscriberFactory(GcpProjectIdProvider)`
  Use `DefaultSubscriberFactory(GcpProjectIdProvider, PubSubConfiguration)` instead

- `PubSubConfiguration.computeSubscriberRetrySettings(String, String)`
  Use `PubSubConfiguration.computeSubscriberRetrySettings(ProjectSubscriptionName)` instead

- `PubSubConfiguration.computeSubscriberFlowControlSettings(String, String)`
  Use `PubSubConfiguration.computeSubscriberFlowControlSettings(ProjectSubscriptionName)` instead

- `PubSubConfiguration.getSubscriber(String, String)`
  Use `PubSubConfiguration.getSubscriptionProperties(ProjectSubscriptionName)` instead

#### Cloud Spanner
- `SpannerPersistentEntityImpl(TypeInformation<T>)`
  Use `SpannerPersistentEntityImpl(TypeInformation, SpannerMappingContext, SpannerEntityProcessor)` instead

- `SpannerCompositeKeyProperty.getPersistentEntityTypes()`
  Use `SpannerCompositeKeyProperty.getPersistentEntityTypeInformation()` instead

#### Cloud Trace
- `TracingSubscriberFactory.createSubscriberStub()`
  Use `TracingSubscriberFactory.createSubscriberStub(String)` instead
