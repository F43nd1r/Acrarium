FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/WEB-INF/lib /acrarium/lib
COPY ${DEPENDENCY}/WEB-INF/classes /acrarium
ENTRYPOINT ["java","-cp","acrarium:acrarium/lib/*","com.faendir.acra.BackendApplication"]