FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

RUN uid=$(($(($((`date +%s` / 60)) % 165535)) + 100000)) && adduser -D -h /application/ -u $uid acrarium && chown -R acrarium:acrarium /application
USER acrarium

ENTRYPOINT java org.springframework.boot.loader.JarLauncher
