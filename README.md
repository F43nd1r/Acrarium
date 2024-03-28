[![Actions Status](https://img.shields.io/github/actions/workflow/status/F43nd1r/Acrarium/tests.yml?label=Tests&style=for-the-badge)](https://github.com/F43nd1r/Acrarium/actions)
[![License](https://img.shields.io/github/license/F43nd1r/Acrarium?style=for-the-badge)](https://github.com/F43nd1r/Acrarium/blob/master/LICENSE)
[![Docker Pulls](https://img.shields.io/docker/pulls/f43nd1r/acrarium?style=for-the-badge)](https://hub.docker.com/repository/docker/f43nd1r/acrarium)
[![Github Downloads](https://img.shields.io/github/downloads/F43nd1r/Acrarium/total?label=Github%20Downloads&style=for-the-badge)](https://github.com/F43nd1r/Acrarium/releases)
[![Latest Version](https://img.shields.io/docker/v/f43nd1r/acrarium?label=Latest%20version&style=for-the-badge)](https://github.com/F43nd1r/Acrarium/releases)

<h1 align=center>
<img src="acrarium/src/main/resources/META-INF/resources/images/logo.png" width=50%>
</h1>

A Backend for [ACRA](https://github.com/ACRA/acra) written in Kotlin using Spring Boot, Vaadin and MySQL

# Setup

See [Wiki Setup guide](https://github.com/F43nd1r/acra-backend/wiki/Setup-guide)

# Screenshots

###### Listing reports

![report list](screenshots/reports.png)

###### Statistics

![statistics](screenshots/statistics.png)

###### Proguard and Export Support

![admin tab](screenshots/admin.png)

###### Report Summary and Attachment Support

![report summary](screenshots/summary.png)

###### Full Report content

![report content](screenshots/details.png)

###### Dark Theme

![dark theme](screenshots/dark.png)

# Development

## Running locally

```shell
./gradlew bootRun
```

## Building release

### Jar

```shell
./gradlew bootJar -x test -Pvaadin.productionMode=true
```

### Docker Image

```shell
./gradlew bootJar -x test -Pvaadin.productionMode=true
docker build -t f43nd1r/acrarium:git acrarium
```

# License

All source code in this repository is licensed under the Apache License 2.0 (see [LICENSE](LICENSE))

# Credits

Thanks to

- [Mirza Zulfan](https://github.com/mirzazulfan) for creating the logo.
- [aptly-io](https://github.com/aptly-io) (Dutch), [Federico Iosue](https://github.com/federicoiosue) (Italian), [Astarivi](https://github.com/astarivi) (
  Spanish), [iisimpler](https://github.com/iisimpler) (Chinese) for contributing to localization.
