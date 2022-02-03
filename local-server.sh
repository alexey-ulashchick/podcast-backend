#!/bin/bash

# Building uber-jar locally
./gradlew shadowJar

# Using compose to spin up  Envoy & Server
docker-compose --env-file src/main/resources/.env -f docker-compose-dev.yml up --build