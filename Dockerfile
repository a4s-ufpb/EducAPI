FROM openjdk:17-jdk
EXPOSE 8080
COPY . ./educapi
WORKDIR /educapi
RUN ./mvnw clean package -DskipTests
ENTRYPOINT ["java", "-jar", "target/EducAPI.jar"]



