FROM adoptopenjdk:8-jre-openj9
# softtrade-base-tzdata-fonts
# FROM tomcat:8-alpine
LABEL MAINTAINER SiritasDho<siritas@gmail.com>

ENV TZ = "Asia/Bangkok"

RUN set -eux; \
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

# EXPOSE 8080
# CMD ["catalina.sh", "run"]
