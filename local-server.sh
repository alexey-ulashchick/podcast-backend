#!/bin/bash

# Building uber-jar locally
./gradlew shadowJar

# Using compose to spin up  Envoy & Server
docker-compose -f docker-compose-dev.yml up --build