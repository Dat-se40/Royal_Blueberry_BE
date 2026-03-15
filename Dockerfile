# Bước 1: Build ứng dụng
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Tạo thư mục và tải model bằng lệnh có sẵn trong môi trường Maven
RUN mkdir -p src/main/resources/models
RUN apt-get update && apt-get install -y curl unzip
RUN curl -L "https://drive.google.com/file/d/1xd_pAioTWO2RIpCwNjDzvDYuSmpu2Qc9/view?usp=drive_link" -o models.zip
RUN unzip -o models.zip -d src/main/resources/models && rm models.zip

# Copy code và build file JAR
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Mở port (thường là 8080 cho Spring Boot)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]