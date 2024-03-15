FROM maven:3.9.6-amazoncorretto-17-al2023 as build
COPY . .
RUN mvn clean
RUN mvn install

FROM amazoncorretto:17.0.10-alpine3.19
WORKDIR /educapi
COPY --from=build target/EducAPI.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]