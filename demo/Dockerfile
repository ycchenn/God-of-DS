# Dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app

# 複製 demo 資料夾內容
COPY . /app

# 確保 Maven Wrapper 可執行
RUN chmod +x ./mvnw

# 進行構建
RUN ./mvnw clean package

# 啟動應用
CMD ["java", "-jar", "target/your-app-name.jar"]

