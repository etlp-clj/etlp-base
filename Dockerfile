# Base Java image for building the application
FROM clojure:lein AS build

RUN apt-get update && apt-get upgrade -y
RUN dpkg -r --force-all apt apt-get && dpkg -r --force-all debconf dpkg
RUN mkdir /app
RUN groupadd -g 999 appuser && useradd -r -u 999 -g  appuser -m appuser
RUN chown -R appuser:appuser /app
USER appuser

# Set the working directory
WORKDIR /app

# Copy the project.clj dependencies files
COPY ./bin /app/bin
COPY ./resources /app/resources
COPY ./src      /app/src
COPY ./test     /app/test

COPY  project.clj /app/

# Download and cache dependencies
RUN ./bin/setup.sh

# Base Java image for running the application
FROM openjdk:11-jre-slim AS run
RUN apt-get update && apt-get upgrade -y
RUN dpkg -r --force-all apt apt-get && dpkg -r --force-all debconf dpkg
RUN mkdir /app
RUN groupadd -g 999 appuser && useradd -r -u 999 -g  appuser -m appuser
RUN chown -R appuser:appuser /app
USER appuser


# Set the working directory
WORKDIR /app

# Copy the JAR file and resources from the build image
COPY --from=build /app/bin/ /app/bin/

# Run the application

ENV ETLP_MAPPER_SERVICE=${ETLP_MAPPER_SERVICE}
ENV ETLP_MAPPER_CSV_KEY=${ETLP_MAPPER_CSV_KEY}
ENV ETLP_MAPPER_CSV_TRANSFORM_KEY=${ETLP_CSV_TRANSFORM_KEY}
ENV ETLP_PROCESS_THREADS=${ETLP_PROCESS_THREADS}
ENV ETLP_WORKER_THREADS=${ETLP_WORKER_THREADS}
ENV ETLP_MAX_MESSAGES_WATERMARK=${ETLP_MAX_MESSAGES_WATERMARK}
