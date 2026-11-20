FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /build/target/*.jar app.jar
USER app
EXPOSE 8765
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]