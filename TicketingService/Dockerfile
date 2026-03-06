# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# 在拷贝源码之前先下载依赖。这样做的好处是：只要 pom.xml 没变，下次构建就会直接利用 Docker 缓存，不需要重新下包，大大加快构建速度。
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests clean package

# ---- Runtime stage ----
    # 换成 JRE (Java Runtime Environment)。它比 JDK 小得多，且更安全（减少了攻击面）。
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=dev

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 6240
EXPOSE 9240
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
