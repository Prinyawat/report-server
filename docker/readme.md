# build base image

```sh
> docker build -f docker/base/Dockerfile -t registry.gitlab.com/softsquare_ssru/registry/report-base .
```

```
> docker build -f docker/base/win.Dockerfile -t registry.gitlab.com/softsquare_ssru/registry/report-base/win .
```

# build reporting image
```
docker build -f docker/Dockerfile -t ssru-report -t registry.gitlab.com/softsquare_ssru/registry/report . 
```