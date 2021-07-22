# Docker-based Measurement Environment

This is a Docker-based environment.
It derives from an official [OpenJDK 7 image](https://hub.docker.com/_/openjdk).
This has been extended with a [customized Defects4J](https://github.com/Frenkymd/defects4j/tree/chain) which is preconfigured to use the instrumenter agent to collect call chains.

## How to Build

```
docker build -t d4j -f Dockerfile ..
```

> **Note:** You may run into a permission denied error when trying to build the image or run the container.
> In this case, run docker as the superuser or add the current user to the `docker` group `sudo usermod -aG docker $USER`.
> Don't forget to log out and log back in so that your group membership is re-evaluated.

## Usage

 1. Checkout an arbitrary bug from Defects4J on the host

```
defects4j checkout -p Time -v 1b -w Time-1b
```

 2. Switch to the given directory and run the measurements using the Docker container

```
cd Time-1b

docker run --rm -it -v "$PWD":/measurement -w /measurement d4j
```

The image is configured to run the `defects4j test` command by default but any valid command can be used, for example:

```
docker run --rm -it -v "$PWD":/measurement -w /measurement d4j defects4j coverage
```
or:
```
docker run --rm -it -v "$PWD":/measurement -w /measurement d4j bash
```

Results will be created on the host in the current working directory (see: `-v "$PWD":/measurement`).

> **Note:** See an additional example in the Jenkins job descriptor XML that can be found in the `REPO_ROOT/jenkins` folder.
