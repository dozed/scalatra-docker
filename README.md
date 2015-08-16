# Scalatra Docker App #

## Prerequisites ##

  - SBT
  - Docker

## Build & Run Docker image ##

```sh
$ cd scalatra-docker
$ sbt
> docker
> ctrl+d
$ docker run -ti \
  -p 8080:80 \
  -v $(pwd)/conf:/app/conf:ro \
  -v $(pwd)/data:/app/data \
  -e CONFIG_FILE=/app/conf/application.conf \
  org.scalatra/scalatra-docker-app
```

## Develop ##

```sh
$ cd scalatra-docker
$ sbt
> container:start
> browse
```
