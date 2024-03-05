FROM amazoncorretto:17.0.10-alpine3.19
WORKDIR /app
ARG JAR_FILE=target/EducAPI.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]