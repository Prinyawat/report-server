FROM openjdk:8-jre-windowsservercore-1809

LABEL MAINTAINER SiritasDho<siritas@gmail.com>

ENV TZ = "Asia/Bangkok"

COPY src/main/resources/fonts/ c:/windows/fonts/

# USER ContainerAdministrator
COPY docker/base/hosts C:/Windows/System32/drivers/etc/

# 
# RUN echo 10.20.21.30 idp.ssru.ac.th idt.ssru.ac.th >> "C:\Windows\System32\drivers\etc\hosts"

# USER ContainerUser