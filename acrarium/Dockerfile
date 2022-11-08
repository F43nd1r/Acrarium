FROM eclipse-temurin:17.0.5_8-jre-alpine as builder
WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17.0.5_8-jre-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
RUN true # https://github.com/moby/moby/issues/37965
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./

RUN uid=$(($(($((`date +%s` / 60)) % 165535)) + 100000)) && adduser -D -h /application/ -u $uid acrarium && chown -R acrarium:acrarium /application
USER acrarium

ENTRYPOINT java org.springframework.boot.loader.JarLauncher
