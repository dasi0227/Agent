# ============== 构建阶段==============
# 创建一个 JDK 17 + Maven 3.9 的环境
FROM maven:3.9-eclipse-temurin-17 AS build
# 指定目录
WORKDIR /app
# 复制 pom
COPY pom.xml .
# 复制源码
COPY src ./src
# 打包项目
RUN mvn -q -DskipTests package

# ============== 运行阶段==============
# 创建一个运行 Java 17 的环境
FROM eclipse-temurin:17-jre
# 指定目录
WORKDIR /app
# 将打包好的包移动到当前目录
COPY --from=build /app/target/mcp-server-csdn.jar /app/mcp-server-csdn.jar
# 指定端口
EXPOSE 9001
# 指定 JVM 参数
ENV JAVA_OPTS=""

# 启动容器的命令
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/mcp-server-csdn.jar"]
