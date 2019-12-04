# Git Fetcher

**Git Fetcher** is a command-line application that will list the commits of any given repository. It uses three methods to retrieve them, in order:

* The Github Public API (https://developer.github.com/v3/)
* An already persisted collection of commits in a database (PostgreSQL), for when the Public API is down
* The git CLI program if everything else fails

## Build

**Git Fetcher** uses Gradle (https://gradle.org/) to build and manage dependencies. In order to build, simply do:

``` shell
$ ./gradlew build
```

The build directory ( *./build/libs* ) should now contain the *git-fetcher.jar* file.

## Run

A PostgreSQL (https://www.postgresql.org/) database is required. If no other instance is already provisioned, use Docker (https://www.docker.com/) to provision a local PostgreSQL and expose it on the default port (5432):

``` shell
$ docker run -d -p 5432:5432 --restart=always --name postgres postgres:9.5.2
```

If there's already a PostgreSQL instance available for use, simply set the required environment variables for Spring Data to pick up. Mind that the application uses the default schema when it creates the commit table.

* `export SPRING_DATASOURCE_URL=`
* `export SPRING_DATASOURCE_USERNAME=`
* `export SPRING_DATASOURCE_PASSWORD=`

And start the application using Gradle Spring Boot tasks:

``` shell
$ ./gradlew bootRun --args "<repo-url>"
```
