# syntax=docker/dockerfile:1
# FROM eclipse-temurin:11-jre-nanoserver-1809 AS runner
FROM eclipse-temurin:11-jre-windowsservercore-1809 AS runner

LABEL org.opencontainers.image.title="SSRU Credit Bank Report Service"
# LABEL org.opencontainers.image.url="https://gitlab.com/softsquare_ssru/registry/container_registry/4413066"
# LABEL org.opencontainers.image.source="https://github.com/softsquare-ssru/smartu-cb-report"
# LABEL org.opencontainers.image.authors="Siritas<siritas_s@softsquaregroup.com>"

COPY src/main/resources/fonts/ c:/windows/fonts/
COPY docker/base/hosts C:/Windows/System32/drivers/etc/

#FROM csanchez/maven:3-amazoncorretto-11-windowsservercore-1809 AS builder
FROM jenkins/jnlp-agent-maven:windows-nanoserver-jdk11 AS builder

ENV MAVEN_OPTS="-Xmx1024m"

WORKDIR /usr/src
COPY pom.xml .

# RUN mvn -B -e -C -T 1C  org.apache.maven.plugins:maven-dependency-plugin:2.10:go-offline
RUN mvn -B -e -C -T 1C dependency:go-offline

COPY src ./src
RUN mvn -B -e -T 1C -DskipTests verify

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
ENV JAVA_OPTS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false -Dfile.encoding=UTF-8 -noverify -server"
# JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Xms512m 
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -XX:+UseG1GC -XX:TieredStopAtLevel=1 -XX:+UnlockExperimentalVMOptions -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=default
ENV SPRING_APPLICATION_JSON='{}'

# VOLUME /tmp
# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ARG DEPENDENCY=c:/usr/src/target/dependency
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/org /app/org
COPY --from=builder ${DEPENDENCY}/WEB-INF /app/WEB-INF

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-cp", "/app;/app/META-INF;/app/WEB-INF/lib/*;/app/WEB-INF/lib-provided/*;/app/WEB-INF/classes;", "com.softsquare.ssru.SpaApplication"]
