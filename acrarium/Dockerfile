FROM alpine:latest as builder

RUN apk --no-cache add openjdk21-jdk openjdk21-jmods binutils
ENV JAVA_MINIMAL="/opt/java-minimal"
RUN /usr/lib/jvm/java-21-openjdk/bin/jlink \
    --verbose \
    --add-modules \
        java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,jdk.unsupported,jdk.zipfs,jdk.crypto.ec \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
    --release-info="add:IMPLEMENTOR=f43nd1r:IMPLEMENTOR_VERSION=f43nd1r_JRE" \
    --output "$JAVA_MINIMAL"

WORKDIR application
ARG JAR_FILE=build/libs/application.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM alpine:latest

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"
COPY --from=builder "$JAVA_HOME" "$JAVA_HOME"

WORKDIR application
COPY --from=builder application/extracted/dependencies/ ./
RUN true # https://github.com/moby/moby/issues/37965
COPY --from=builder application/extracted/spring-boot-loader/ ./
RUN true
COPY --from=builder application/extracted/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/extracted/application/ ./

RUN adduser -D -h /application/ acrarium && chown -R acrarium:acrarium /application
USER acrarium

ENTRYPOINT java -jar application.jar
