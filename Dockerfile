FROM openjdk:11-jdk
EXPOSE 8080
COPY . ./educapi
WORKDIR /educapi
RUN ./mvnw clean package -DskipTests
ENTRYPOINT ["java", "-jar", "target/EducAPI.jar"]



