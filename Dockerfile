#Utilizando imagem base o OpenJDK com Maven
FROM maven:3.8.2-openjdk-17-slim AS build

#Criando diretório para o build do projeto
WORKDIR /build

#Copiando o pom.xml para o container e baixando as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

#Copiando o código fonte e fazendo o build
COPY src ./src
RUN mvn package -DskipTests

#Utilizando uma imagem base do OpenJDK na versão 17
FROM openjdk:17-slim

#Criando diretório do projeto
WORKDIR /educapi

#Copiando o arquivo jar do diretório de build
COPY --from=build /build/target/*.jar app.jar

#Expondo a porta 8080 para acessar o projeto
EXPOSE 8080

#Executando comando para rodar o projeto
CMD ["java", "-jar", "app.jar"]