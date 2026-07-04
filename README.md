# Docker Command Builder

A small Spring Boot web app with a form-based UI for building `docker` CLI
commands. Fill in image, ports, volumes, env vars, and other options; the
right-hand terminal panel shows the generated command live and lets you copy
it straight to your clipboard.

## What it covers

- Base subcommand: `run`, `create`, `exec`, `build`, `start`, `stop`,
  `restart`, `rm`, `rmi`, `logs`, `ps`, `pull`, `push`
- Image / container name
- Port mappings (`-p host:container[/proto]`, `-P`)
- Volume / bind mounts (`-v host:container[:ro|rw]`)
- Environment variables (`-e KEY=VALUE`)
- Labels (`--label key=value`)
- Detached / interactive-tty / auto-remove flags (`-d`, `-it`, `--rm`)
- Network (`--network`) and restart policy (`--restart`)
- Working dir, user, hostname, entrypoint (`-w`, `-u`, `-h`, `--entrypoint`)
- CPU / memory limits (`--cpus`, `-m`)
- Trailing command/args, plus a free-text "extra flags" field for anything
  not covered above

The form automatically shows/hides the fields that make sense for whichever
base subcommand you pick.

## Project structure

```
docker-command-builder/
├── pom.xml
└── src/main/
    ├── java/com/toolsmith/dockerbuilder/
    │   ├── DockerCommandBuilderApplication.java   # entry point
    │   ├── controller/                            # REST endpoint
    │   ├── service/DockerCommandService.java       # builds the command string
    │   └── model/                                  # request/response DTOs
    └── resources/
        ├── application.properties
        └── static/                                 # HTML/CSS/JS front end
            ├── index.html
            ├── css/style.css
            └── js/app.js
```

The frontend calls `POST /api/generate` with the form state as JSON and gets
back `{ "command": "docker run -p 8080:80 nginx:latest" }`.

## Running it locally

Requires JDK 17+ and Maven.

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080**.

Or build a runnable jar:

```bash
mvn clean package
java -jar target/docker-command-builder.jar
```

Change the port in `src/main/resources/application.properties`
(`server.port=8080`) or override it at launch:

```bash
java -jar target/docker-command-builder.jar --server.port=9090
```

## Deploying / hosting later

Because it's a single self-contained Spring Boot jar with no database, it
can be hosted almost anywhere:

- **Any VM / VPS**: `scp` the built jar over, run it with `java -jar`, and
  put it behind Nginx/Caddy for TLS, or run it as a `systemd` service.
- **Docker**: containerize it yourself, e.g.

  ```dockerfile
  FROM eclipse-temurin:17-jre
  COPY target/docker-command-builder.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "/app.jar"]
  ```

  Build and run:

  ```bash
  mvn clean package
  docker build -t docker-command-builder .
  docker run -p 8080:8080 docker-command-builder
  ```

  (Kind of fitting, using this very app to help you write that `docker run`
  command once it's live.)

- **PaaS options** (Render, Railway, Fly.io, Heroku-likes, AWS Elastic
  Beanstalk, Azure App Service): most auto-detect a Maven/Spring Boot project
  or accept the Dockerfile above directly.

## Extending it

- Add new form fields in `index.html`, wire them up in `js/app.js`
  (`buildPayload()`), and add matching fields to
  `model/DockerCommandRequest.java` + the flag logic in
  `service/DockerCommandService.java`.
- To support a `docker-compose` style output instead, add a new endpoint
  and service method — the DTOs already capture most of what a compose
  service definition needs (ports, volumes, environment, etc.).
