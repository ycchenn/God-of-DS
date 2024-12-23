FROM eclipse-temurin:21-jdk

# 設定工作目錄
WORKDIR /app

# 複製專案文件
COPY . /app

# 確保 Maven Wrapper 可執行
RUN chmod +x ./mvnw

# 進行構建
RUN ./mvnw clean package

# 啟動應用
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
