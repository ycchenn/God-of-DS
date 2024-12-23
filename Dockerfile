# 使用支援 Java 21 的基礎映像
FROM eclipse-temurin:21-jdk

# 設定容器內的工作目錄
WORKDIR /app

# 複製專案檔案到容器內
COPY . /app

# 確保 Maven Wrapper 文件有執行權限
RUN chmod +x ./mvnw

# 確保 Maven Wrapper 配置完整（重新生成 maven-wrapper.properties）
RUN ./mvnw -N io.takari:maven:wrapper

# 使用 Maven Wrapper 進行建構
RUN ./mvnw package

# 設定啟動命令，啟動生成的 JAR 檔案
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
