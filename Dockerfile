
# =========================================
# BUILD STAGE
# =========================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY . .

RUN chmod +x ./mvnw \
    && ./mvnw -B -DskipTests clean package \
    && cp target/quanlydaotao-0.0.1-SNAPSHOT.jar app.jar


# =========================================
# RUNTIME STAGE
# =========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

ENV PORT=10000

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar"]
