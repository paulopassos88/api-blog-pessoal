# Estágio de Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copia apenas os arquivos necessários para o Gradle baixar as dependências
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Dá permissão de execução ao gradlew e baixa as dependências (cache)
RUN ./gradlew dependencies --no-daemon

# Copia o código fonte e gera o jar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Estágio de Execução
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Cria um usuário não-root para segurança
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copia o JAR do estágio de build
COPY --from=build /app/build/libs/*.jar app.jar

# Configurações de JVM recomendadas para containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
