# Reporting Service

**Testing URL**

`http://localhost:8080/report/reportTest?lang_code=th&fac_code=00&reportName=test&exportType=pdf`

working database connection needed
 
 ### build Docker image; reporting server

 ```
 docker build -f docker/Dockerfile -t ssru-report -t registry.gitlab.com/softsquare_ssru/registry/reportsvr .
 ```

### Enable Docker Buildkit features

>
> Since Docker 18.09, BuildKit is an opt-in feature that can be enabled by setting "features": {"buildkit": true} into the daemon.json file, or with setting the environment variable DOCKER_BUILDKIT=1 before running docker build.
> If you're using some older versions (18.06 or recent), you've to add "experimental": true into the daemon.json file and set the environment variable DOCKER_BUILDKIT=1 before the build.
>

ดูที่นี่เพ่ิมเดิม [Build images with BuildKit](https://docs.docker.com/develop/develop-images/build_enhancements/)

**ถ้าไม่สามารถใช้ docker buildkit ได้ให้**

ลบบรรทัดแรกออก

```
# syntax=docker/dockerfile:experimental
...
```

ใช้ line ที่ comment อยู่ข้างล่่างแทน line ข้างบนของมัน

```
RUN --mount=type=cache,target=/root/.m2 mvn -B -e -fn -C -T 1C dependency:go-offline
# RUN mvn -B -e -fn -C -T 1C dependency:go-offline
```

เปลี่ยน identity server ด้วย 2 json ค่านี้
```
"spring.security.oauth2.resourceserver.jwt.issuer-uri":"https://spa.softsquaregroup.com/ssru.identity/.well-known/openid-configuration",
"spring.security.oauth2.resourceserver.jwt.jwk-set-uri":"https://spa.softsquaregroup.com/ssru.identity/.well-known/openid-configuration/jwks"}
```