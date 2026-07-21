# Usa a imagem do Java 17
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos do projeto
COPY . .

# Dá permissão de execução para o wrapper do Maven
RUN chmod +x ./mvnw

# Compila o projeto
RUN ./mvnw clean package -DskipTests

# Estágio de execução
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Roda a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]