# 使用 Eclipse Temurin 作為基底映像
FROM eclipse-temurin:21-jdk

# 設置工作目錄
WORKDIR /app

# 複製當前目錄的所有文件到映像中的工作目錄
COPY . /app

# 確保 Maven Wrapper 可執行
RUN chmod +x ./mvnw

# 使用 Maven Wrapper 構建應用
RUN ./mvnw clean package -DskipTests

# 暴露應用程序的運行端口
EXPOSE 8080

# 設定啟動命令，啟動構建後的 Spring Boot 應用程式
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
