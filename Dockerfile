FROM amazoncorretto:17.0.10-alpine3.19
EXPOSE 8080
COPY . ./educapi
WORKDIR /educapi
RUN ./mvnw clean
RUN ./mvnw test
RUN ./mvnw install
ENTRYPOINT ["java", "-jar", "target/EducAPI.jar"]