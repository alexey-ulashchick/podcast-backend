FROM gradle:7.1.1-jdk8-openj9 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar

FROM openjdk:8-jre-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/dashboard-service-auth-1.0.0-all.jar /app/application.jar
ENTRYPOINT ["java", "-jar","/app/application.jar"]