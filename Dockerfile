#сборка
FROM gradle:8.6 as build

WORKDIR /app

COPY . .

RUN gradle build

#запуск
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]