FROM amazoncorretto:17-alpine-jdk as builder

WORKDIR /app
COPY . .
RUN ./gradlew build -x test

FROM amazoncorretto:17-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]