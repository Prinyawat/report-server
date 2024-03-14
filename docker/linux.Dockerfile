# syntax=docker/dockerfile:1
FROM maven:3-eclipse-temurin-11-alpine AS build
FROM eclipse-temurin:11-jre-jammy AS runner

LABEL org.opencontainers.image.title="Service Price System Report Service"
# LABEL org.opencontainers.image.url="https://gitlab.com/softsquare_ssru/registry/container_registry/4413066"
# LABEL org.opencontainers.image.source="https://github.com/softsquare-ssru/smartu-cb-report"
# LABEL org.opencontainers.image.authors="Siritas<siritas_s@softsquaregroup.com>"

ENV TZ = "Asia/Bangkok"
# workaround for ubuntu 22.04 `apt-get` command
ENV GNUTLS_CPUID_OVERRIDE = "0x1"

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        curl \
        unzip \
        fontconfig \
        tzdata
RUN echo "Asia/Bangkok" | tee /etc/timezone && \
    dpkg-reconfigure --frontend noninteractive tzdata
COPY src/main/resources/fonts /tmp/

RUN mkdir -p /usr/share/fonts/ \
    && cp /tmp/*.ttf /usr/share/fonts/ \
    && fc-cache -f

RUN rm -rf /var/lib/apt/lists/* \
    && rm -f /tmp/*.ttf

FROM build AS builder

ENV MAVEN_OPTS="-Xmx1024m"

WORKDIR /workspace/
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 mvn -B -e -fn -C -T 1C dependency:go-offline
# RUN mvn -B -e -fn -C -T 1C dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -e -T 1C -DskipTests verify
# RUN mvn -B -e -o -T 1C -DskipTests verify

FROM runner AS final

ARG IMAGE_CREATED
ARG IMAGE_VERSION
ARG IMAGE_REVISION

LABEL org.opencontainers.image.created="$IMAGE_CREATED"
#version of the packaged software
LABEL org.opencontainers.image.version="$IMAGE_VERSION"
#Source control revision identifier
LABEL org.opencontainers.image.revision="$IMAGE_REVISION"

ARG JAVA_OPTS
ARG JAVA_TOOL_OPTIONS
ENV JAVA_OPTS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false -XX:+UseG1GC -XX:TieredStopAtLevel=1 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dfile.encoding=UTF-8 -noverify -server"
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -XX:TieredStopAtLevel=1 -XX:+UnlockExperimentalVMOptions -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=default
ENV SPRING_APPLICATION_JSON='{}'

# VOLUME /tmp
# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ARG DEPENDENCY=/workspace/target/dependency
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/org /app/org
COPY --from=builder ${DEPENDENCY}/WEB-INF /app/WEB-INF

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-cp", "app:app/WEB-INF/classes:app/WEB-INF/lib/*", "com.softsquare.report.SpaApplication"]
