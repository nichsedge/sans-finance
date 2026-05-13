# Build stage
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Disable configuration cache for the build to avoid issues in Docker
RUN ./gradlew :shared:wasmJsBrowserDistribution --no-configuration-cache
RUN ./gradlew :server:installDist --no-configuration-cache

# Run stage
FROM openjdk:17-slim
EXPOSE 8080
WORKDIR /app
COPY --from=build /home/gradle/src/server/build/install/server /app
COPY --from=build /home/gradle/src/shared/build/dist/wasmJs/productionExecutable /app/web
ENTRYPOINT ["/app/bin/server"]
