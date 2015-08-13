# Scalatra Docker App #

## Build & Run Docker image ##

```sh
$ cd scalatra-docker
$ ./sbt
> docker
> ctrl+d
$ docker run -ti -p 8080:80 -v $(pwd)/logs:/app/logs org.scalatra/scalatra-docker-app
```

## Develop ##

```sh
$ cd scalatra-docker
$ ./sbt
> container:start
> browse
```
