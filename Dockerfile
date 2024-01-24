FROM openjdk:11-jdk
EXPOSE 8080
COPY . ./educapi
WORKDIR /educapi
RUN ./mvnw clean
RUN ./mvnw test
RUN ./mvnw install
ENTRYPOINT ["java", "-jar", "target/EducAPI.jar"]



