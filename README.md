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

## Building a Windows installer (.exe)

Same command-generation logic and UI as the web app, packaged as a native
Windows installer using `jpackage` — bundles its own JRE, so nobody
installing it needs Java. It launches with the auto-open-browser code
already in `DockerCommandBuilderApplication.java`, so double-clicking the
installed app opens straight to the tool in the default browser.

### Option A — GitHub Actions (no Windows machine needed)

`.github/workflows/build-windows-exe.yml` builds the `.exe` automatically
on a real Windows runner in the cloud every time you push to `main` or
`Pritam`. To get the file:

1. Push this repo (with the workflow file) to GitHub.
2. Go to your repo's **Actions** tab → **Build Windows Installer** → wait
   for the run to finish (few minutes).
3. Open the completed run → scroll to **Artifacts** →
   download `docker-command-builder-windows-installer`.
4. Unzip it — inside is `Docker Command Builder-1.0.exe`.
5. Copy that to a Windows machine and double-click to install. It adds a
   Start Menu shortcut and desktop icon, both using the whale/hard-hat icon
   from `installer-assets/icon.ico`.

You can also trigger a build manually any time from the **Actions** tab →
**Run workflow**, without needing a new push.

### Option B — building it yourself on an actual Windows PC

Requires JDK 17+ and the [WiX Toolset v3](https://wixtoolset.org/) (jpackage
needs WiX to produce `.exe`/`.msi` on Windows):

```powershell
mvn clean package

jpackage `
  --input target `
  --main-jar docker-command-builder.jar `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --name "Docker Command Builder" `
  --app-version 1.0 `
  --type exe `
  --icon installer-assets\icon.ico `
  --win-shortcut `
  --win-menu `
  --win-dir-chooser `
  --dest dist
```

The installer lands in `dist\Docker Command Builder-1.0.exe`.

### Deploying on Render specifically


Render has no native Java buildpack, so it deploys this app from the
`Dockerfile` already included in this repo. Steps:

1. Push this project (including the `Dockerfile`) to your GitHub repo.
2. In the Render dashboard: **New +** → **Web Service**.
3. Choose **Build and deploy from a Git repository**, connect GitHub, and
   select this repo.
4. Render should auto-detect the `Dockerfile` and set **Environment** /
   **Runtime** to **Docker**. If it doesn't, set it manually.
5. Leave **Build Command** and **Start Command** blank — the Dockerfile
   handles both.
6. Pick the **Free** instance type (or whatever plan you want) and click
   **Create Web Service**.
7. Render assigns a `PORT` env var automatically at runtime;
   `application.properties` already reads it via `server.port=${PORT:8080}`,
   so no extra config is needed there.
8. Wait for the build/deploy logs to finish — Render gives you a
   `https://<your-service-name>.onrender.com` URL when it's live.

Every subsequent `git push` to the connected branch triggers an automatic
redeploy.

## Extending it

- Add new form fields in `index.html`, wire them up in `js/app.js`
  (`buildPayload()`), and add matching fields to
  `model/DockerCommandRequest.java` + the flag logic in
  `service/DockerCommandService.java`.
- To support a `docker-compose` style output instead, add a new endpoint
  and service method — the DTOs already capture most of what a compose
  service definition needs (ports, volumes, environment, etc.).
