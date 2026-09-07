# Database configuration

Acrarium includes both MySQL Connector/J and MariaDB Connector/J in its executable JAR, WAR and Docker image. Spring Boot selects the driver from the JDBC URL and detects the jOOQ dialect from the resulting connection. No extra JDBC JAR, classpath configuration or alternative launcher is needed.

## MariaDB

```yaml
spring:
  datasource:
    url: jdbc:mariadb://db:3306/acrarium
    username: acrarium
    password: ${ACRARIUM_DATABASE_PASSWORD}
```

Equivalent environment variables (also usable with the Docker image):

```shell
export SPRING_DATASOURCE_URL='jdbc:mariadb://db:3306/acrarium'
export SPRING_DATASOURCE_USERNAME='acrarium'
export SPRING_DATASOURCE_PASSWORD="$ACRARIUM_DATABASE_PASSWORD"
java -jar acrarium/build/libs/application.jar
```

The expected driver is `org.mariadb.jdbc.Driver` and the dialect is `MARIADB`.

For an existing MariaDB installation configured with `jdbc:mysql:`, change the URL prefix to `jdbc:mariadb:` and review driver-specific URL options for MariaDB Connector/J. Keep the database host, port and database name appropriate to that instance. Remove any explicit `com.mysql.cj.jdbc.Driver` setting and any `MYSQL` dialect override. Remove the obsolete `spring.jooq.dialect` setting: Acrarium does not consume it and it is not Spring Boot's dialect property. A dialect override alone does not change the JDBC driver.

Review your environment variables, command-line options, Spring Boot `application.properties`/`application.yml` files, and Acrarium's additional files under `~/.config/acrarium/` and `~/.acra/`. An old explicit setting in any active source can override automatic selection. Acrarium does not rewrite your connection URL.

Explicit overrides remain supported when needed:

```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
  jooq:
    sql-dialect: MARIADB
```

Their environment-variable names are `SPRING_DATASOURCE_DRIVERCLASSNAME` and `SPRING_JOOQ_SQLDIALECT`. These are optional; the URL-based setup above is preferred. Changing only `SPRING_JOOQ_SQLDIALECT` while keeping MySQL Connector/J can fail when reading `INSERT ... RETURNING` results.

## MySQL

Use the MySQL URL for an actual MySQL server:

```yaml
spring:
  datasource:
    url: jdbc:mysql://db:3306/acrarium
    username: acrarium
    password: ${ACRARIUM_DATABASE_PASSWORD}
```

The corresponding environment variables are `SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/acrarium`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. The expected driver is `com.mysql.cj.jdbc.Driver` and the dialect is `MYSQL`. If explicitly configured, use `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver` and `spring.jooq.sql-dialect=MYSQL` (or the same environment-variable names shown above).

Configure each installation for its own database engine. These settings do not require changes to Nginx or the application's HTTP port.

## Compatibility tests and builds

The database compatibility tests use separate `mysql:8.0.39` and `mariadb:11.8.3` containers. They exercise the shipped defaults and environment-variable binding, report JSON and generated IDs, concurrent distinct bugs, version upserts, transaction rollback and reopening a populated database with Liquibase. Docker and Java 21 are required, including for the existing build-time MySQL schema generator. Schema generation remains independent of runtime dialect selection.

```shell
./gradlew :acrarium:test --tests '*DatabaseCompatibilityTest'
./gradlew test bootJar bootWar -Pvaadin.productionMode=true
jar tf acrarium/build/libs/application.jar | grep -E 'BOOT-INF/lib/(mysql-connector-j|mariadb-java-client)-'
jar tf acrarium/build/libs/application.war | grep -E 'WEB-INF/lib/(mysql-connector-j|mariadb-java-client)-'
docker build -t acrarium:mariadb-fix acrarium
```

The executable artifacts are `acrarium/build/libs/application.jar` and `acrarium/build/libs/application.war`. Run the JAR with `java -jar`; the Dockerfile's default entrypoint also uses `java -jar`. Supply database settings to Docker with `-e SPRING_DATASOURCE_URL -e SPRING_DATASOURCE_USERNAME -e SPRING_DATASOURCE_PASSWORD` after exporting their values. The database host in the URL must be reachable from that container.

The current build uses the jOOQ Open Source Edition, version 3.21.8. Its [database support matrix](https://www.jooq.org/download/support-matrix) lists MariaDB 11.5 and MySQL 8.0.31 as the minimum versions for the 3.21 OSS dialects. The test images meet those baselines. These tests do not establish compatibility with every MariaDB version, particularly older versions outside jOOQ's supported range. The MariaDB server version from the original bug report is unknown.
