# 使用支援 Java 21 的基礎映像
FROM eclipse-temurin:21-jdk

# 設定容器內的工作目錄
WORKDIR /app

# 複製專案檔案到容器內
COPY . /app

# 使用 Maven Wrapper 進行建構
RUN ./mvnw package

# 設定啟動命令，啟動生成的 JAR 檔案
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
