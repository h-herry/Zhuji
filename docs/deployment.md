# 筑基 (Zhuji) 部署运维手册

## 目录
1. [部署架构](#1-部署架构)
2. [环境准备](#2-环境准备)
3. [Docker 部署](#3-docker-部署)
4. [Kubernetes 部署](#4-kubernetes-部署)
5. [配置管理](#5-配置管理)
6. [监控运维](#6-监控运维)
7. [日志管理](#7-日志管理)
8. [备份恢复](#8-备份恢复)
9. [故障排查](#9-故障排查)
10. [性能调优](#10-性能调优)

---

## 1. 部署架构

### 1.1 生产环境架构图
```
┌─────────────────────────────────────────────────────────────────┐
│                          用户流量                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                      负载均衡 (Nginx/SLB)                        │
│           SSL 终止 / 健康检查 / 负载分发                          │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                  API 网关集群 (Spring Cloud Gateway)               │
│          2+ 实例，无状态，可水平扩展                             │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                    微服务集群 (Microservices)                      │
│  ┌──────────┬──────────┬──────────┬──────────┐              │
│  │用户组织   │ 多语言    │ API集成   │ 工作流   │              │
│  │服务      │ 服务      │ 服务      │ 服务     │              │
│  │(2实例)  │(2实例)    │(2实例)    │(2实例)   │              │
│  └──────────┴──────────┴──────────┴──────────┘              │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                      数据层 (Data Layer)                          │
│  ┌──────────┬──────────┬──────────┬──────────┐              │
│  │ MySQL    │  Redis   │ RabbitMQ │  Nacos   │              │
│  │(主从)    │ (Cluster)│ (Cluster) │ (Cluster) │              │
│  └──────────┴──────────┴──────────┴──────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 高可用设计
| 组件 | 高可用方案 | 说明 |
|------|------------|------|
| API 网关 | 多实例 + 负载均衡 | 无状态，可水平扩展 |
| 微服务 | 多实例 + 注册中心 | 服务自动发现，故障自动摘除 |
| MySQL | 主从复制 + 读写分离 | 主库故障自动切换从库 |
| Redis | Redis Cluster / Sentinel | 数据分片 + 故障自动切换 |
| RabbitMQ | 镜像队列 + 集群 | 队列数据多节点复制 |
| Nacos | 集群部署 + Raft | 数据一致性保证 |

---

## 2. 环境准备

### 2.1 基础设施要求
| 资源 | 最低配置 | 推荐配置 | 说明 |
|------|---------|---------|------|
| CPU | 8 核 | 16 核 | 取决于并发量 |
| 内存 | 16 GB | 32 GB | JVM + 中间件 |
| 磁盘 | 100 GB SSD | 500 GB SSD | 系统 + 日志 + 数据 |
| 网络 | 千兆网卡 | 万兆网卡 | 服务间通信 |
| 操作系统 | Linux (CentOS 7+ / Ubuntu 20.04+) | 同左 | 稳定、安全 |

### 2.2 软件环境
| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | LTS 版本 |
| Docker | 24.x+ | 容器运行时 |
| Kubernetes | 1.28+ | 容器编排 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.x+ | 缓存 + 分布式锁 |
| RabbitMQ | 3.12+ | 消息队列 |
| Nacos | 2.3+ | 服务注册 + 配置中心 |
| Nginx | 1.24+ | 负载均衡 |

### 2.3 网络规划
| 网络 | CIDR | 说明 |
|------|------|------|
| 公网 | - | 用户访问 |
| 负载均衡网络 | 192.168.10.0/24 | 负载均衡器 |
| 应用网络 | 192.168.20.0/24 | 应用服务器 |
| 数据网络 | 192.168.30.0/24 | 数据库、缓存 |
| 管理网络 | 192.168.40.0/24 | 监控、日志 |

---

## 3. Docker 部署

### 3.1 构建镜像
**Maven 插件配置** (`pom.xml`):
```xml
<plugin>
    <groupId>com.spotify</groupId>
    <artifactId>dockerfile-maven-plugin</artifactId>
    <version>1.4.13</version>
    <configuration>
        <repository>registry.zhuji.com/${project.artifactId}</repository>
        <tag>${project.version}</tag>
        <buildArgs>
            <JAR_FILE>target/${project.artifactId}-${project.version}.jar</JAR_FILE>
        </buildArgs>
    </configuration>
</plugin>
```

**Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**构建命令**:
```bash
# 构建单个模块
mvn clean package dockerfile:build -DskipTests

# 构建所有模块
mvn clean package dockerfile:build -DskipTests -pl user-org-service,workflow-service
```

### 3.2 Docker Compose 部署
**`docker-compose.yml`**:
```yaml
version: '3.8'

services:
  # MySQL
  mysql:
    image: mysql:8.0
    container_name: zhuji-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: zhuji
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - zhuji-network

  # Redis
  redis:
    image: redis:7-alpine
    container_name: zhuji-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - zhuji-network

  # Nacos
  nacos:
    image: nacos/nacos-server:v2.3.0
    container_name: zhuji-nacos
    environment:
      - PREFER_HOST_MODE=hostname
      - MODE=standalone
    ports:
      - "8848:8848"
    networks:
      - zhuji-network

  # 用户组织服务
  user-org-service:
    image: registry.zhuji.com/user-org-service:1.0.0
    container_name: zhuji-user-org
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=nacos:8848
    ports:
      - "8081:8080"
    depends_on:
      - mysql
      - redis
      - nacos
    networks:
      - zhuji-network

  # API 网关
  api-gateway:
    image: registry.zhuji.com/api-gateway:1.0.0
    container_name: zhuji-gateway
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR=nacos:8848
    ports:
      - "8080:8080"
    depends_on:
      - nacos
      - user-org-service
    networks:
      - zhuji-network

networks:
  zhuji-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
```

**启动命令**:
```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f user-org-service

# 停止所有服务
docker-compose down
```

---

## 4. Kubernetes 部署

### 4.1 命名空间
**`namespace.yaml`**:
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: zhuji-prod
```

### 4.2 ConfigMap (配置)
**`configmap.yaml`**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-org-service-config
  namespace: zhuji-prod
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:mysql://mysql:3306/zhuji?useSSL=false&serverTimezone=UTC
        username: root
        password: root123
      redis:
        host: redis
        port: 6379
    logging:
      level:
        com.zhuji: DEBUG
```

### 4.3 Deployment (部署)
**`deployment.yaml`**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-org-service
  namespace: zhuji-prod
spec:
  replicas: 2
  selector:
    matchLabels:
      app: user-org-service
  template:
    metadata:
      labels:
        app: user-org-service
    spec:
      containers:
      - name: user-org-service
        image: registry.zhuji.com/user-org-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: SPRING_CLOUD_NACOS_DISCOVERY_SERVER-ADDR
          value: "nacos:8848"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 4.4 Service (服务)
**`service.yaml`**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-org-service
  namespace: zhuji-prod
spec:
  selector:
    app: user-org-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### 4.5 Ingress (入口)
**`ingress.yaml`**:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: zhuji-ingress
  namespace: zhuji-prod
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.zhuji.com
    http:
      paths:
      - path: /api/v1/users
        pathType: Prefix
        backend:
          service:
            name: user-org-service
            port:
              number: 80
```

### 4.6 部署命令
```bash
# 创建命名空间
kubectl apply -f namespace.yaml

# 创建 ConfigMap
kubectl apply -f configmap.yaml

# 部署服务
kubectl apply -f deployment.yaml

# 创建 Service
kubectl apply -f service.yaml

# 创建 Ingress
kubectl apply -f ingress.yaml

# 查看 Pod 状态
kubectl get pods -n zhuji-prod

# 查看日志
kubectl logs -f <pod-name> -n zhuji-prod

# 扩容
kubectl scale deployment user-org-service --replicas=3 -n zhuji-prod
```

---

## 5. 配置管理

### 5.1 Nacos 配置中心
**配置格式**:
```yaml
# 在 Nacos 控制台创建配置
Data ID: user-org-service.yml
Group: DEFAULT_GROUP
Content:
  spring:
    datasource:
      url: jdbc:mysql://mysql:3306/zhuji?useSSL=false
      username: root
      password: root123
  mybatis-plus:
    configuration:
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

**Spring Boot 集成**:
```yaml
# bootstrap.yml
spring:
  application:
    name: user-org-service
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        file-extension: yaml
        refresh-enabled: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
```

### 5.2 配置优先级
1. 命令行参数 (`--spring.datasource.url=...`)
2. Java 系统属性 (`-Dspring.datasource.url=...`)
3. 操作系统环境变量 (`SPRING_DATASOURCE_URL`)
4. Nacos 配置中心
5. 应用内 `application.yml`
6. 应用内 `application.properties`

### 5.3 敏感配置加密
**使用 Jasypt 加密**:
```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

**加密命令**:
```bash
java -cp jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="mysecret" password="encryptionkey" algorithm=PBEWithMD5AndDES
```

**配置文件中使用**:
```yaml
spring:
  datasource:
    password: ENC(encrypted-string)
```

---

## 6. 监控运维

### 6.1 Spring Boot Actuator
**依赖**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**配置**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 6.2 Prometheus + Grafana
**Prometheus 配置** (`prometheus.yml`):
```yaml
scrape_configs:
  - job_name: 'zhuji-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'user-org-service:8080'
        - 'workflow-service:8080'
```

**Grafana Dashboard**:
- 导入社区 Dashboard: [Spring Boot Dashboard](https://grafana.com/grafana/dashboards/10280)
- 自定义 Dashboard: JVM、QPS、RT、错误率

### 6.3 SkyWalking APM
**Agent 配置**:
```bash
java -javaagent:/path/to/skywalking-agent.jar \
     -Dskywalking.agent.service_name=user-org-service \
     -Dskywalking.collector.backend_service=oap:11800 \
     -jar user-org-service.jar
```

**查看链路**:
- 访问 SkyWalking UI: `http://skywalking-ui:8080`
- 查看服务拓扑、链路追踪、性能分析

---

## 7. 日志管理

### 7.1 ELK 日志平台
**Logback 配置** (`logback-spring.xml`):
```xml
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"app_name":"user-org-service"}</customFields>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="LOGSTASH" />
    </root>
</configuration>
```

**Logstash 配置** (`logstash.conf`):
```
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  json {
    source => "message"
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "zhuji-logs-%{+YYYY.MM.dd}"
  }
}
```

### 7.2 日志规范
**日志级别**:
- `ERROR`: 错误日志，需立即处理
- `WARN`: 警告日志，需关注
- `INFO`: 信息日志，关键业务操作
- `DEBUG`: 调试日志，排查问题

**日志格式**:
```
[2026-05-20 10:00:00.123] [INFO] [user-org-service] [traceId=xxx] [spanId=yyy] [userId=1001] - 用户登录成功，username=zhangsan
```

**MDC (Mapped Diagnostic Context)**:
```java
@Aspect
@Component
public class LogAspect {
    @Before("execution(* com.zhuji..*.*(..))")
    public void before(JoinPoint joinPoint) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("userId", getUserId());
    }
}
```

---

## 8. 备份恢复

### 8.1 数据库备份
**MySQL 备份脚本** (`backup-mysql.sh`):
```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/data/backup/mysql"
mysqldump -h mysql -u root -p${MYSQL_ROOT_PASSWORD} --all-databases > ${BACKUP_DIR}/backup_${DATE}.sql
gzip ${BACKUP_DIR}/backup_${DATE}.sql

# 删除 7 天前的备份
find ${BACKUP_DIR} -name "backup_*.sql.gz" -mtime +7 -delete
```

**定时任务** (crontab):
```
0 2 * * * /opt/scripts/backup-mysql.sh >> /var/log/backup.log 2>&1
```

### 8.2 数据库恢复
**恢复命令**:
```bash
# 解压备份文件
gunzip backup_20260520_020000.sql.gz

# 恢复数据
mysql -h mysql -u root -p < backup_20260520_020000.sql
```

### 8.3 Redis 备份
**RDB 持久化** (`redis.conf`):
```
save 900 1     # 900 秒内至少 1 次写操作
save 300 10    # 300 秒内至少 10 次写操作
save 60 10000  # 60 秒内至少 10000 次写操作
dir /data/redis
dbfilename dump.rdb
```

**AOF 持久化**:
```
appendonly yes
appendfsync everysec
```

---

## 9. 故障排查

### 9.1 常见问题

**问题 1: 服务注册失败**
```
# 检查 Nacos 是否可用
curl http://nacos:8848/nacos/actuator/health

# 检查网络连通性
ping nacos
telnet nacos 8848

# 查看服务日志
kubectl logs -f <pod-name> -n zhuji-prod
```

**问题 2: 数据库连接池耗尽**
```
# 查看数据库连接数
SHOW PROCESSLIST;

# 增加连接池配置
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
```

**问题 3: Redis 缓存雪崩**
```
# 解决方案：缓存过期时间加上随机值
int expireTime = 3600 + new Random().nextInt(300); // 3600-3900 秒
redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
```

### 9.2 排查工具

**JVM 问题排查**:
```bash
# 查看 JVM 参数
jinfo -flags <pid>

# 查看堆内存使用
jmap -heap <pid>

# 生成堆转储
jmap -dump:format=b,file=heap.hprof <pid>

# 查看线程栈
jstack <pid> > thread.txt
```

** Arthas 在线诊断**:
```bash
# 启动 Arthas
java -jar arthas-boot.jar

# 查看方法调用耗时
trace com.zhuji.UserService getUserById

# 查看 JVM 状态
dashboard
```

---

## 10. 性能调优

### 10.1 JVM 调优
**推荐配置** (`jvm.options`):
```
-Xms2g                # 初始堆大小
-Xmx2g                # 最大堆大小
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
-XX:+UseG1GC          # 使用 G1 垃圾收集器
-XX:MaxGCPauseMillis=200
-Xlog:gc*:file=/logs/gc.log:time,uptime,level,tags
```

### 10.2 数据库连接池调优
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20       # 最大连接数
      minimum-idle: 5             # 最小空闲连接
      connection-timeout: 30000    # 连接超时时间（毫秒）
      idle-timeout: 600000        # 空闲连接超时时间
      max-lifetime: 1800000       # 连接最大生命周期
```

### 10.3 Redis 调优
```
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru    # 内存满时淘汰最近最少使用的键
timeout 300                     # 客户端超时时间（秒）
tcp-keepalive 60                # TCP 保活时间
```

### 10.4 Tomcat 调优
```yaml
server:
  tomcat:
    threads:
      max: 200                  # 最大线程数
      min-spare: 10             # 最小空闲线程
    connection-timeout: 5000     # 连接超时时间（毫秒）
    keep-alive-timeout: 10000   # Keep-Alive 超时时间
```

---

## 附录

### A. 部署检查清单
- [ ] 所有服务已启动且健康
- [ ] 数据库主从同步正常
- [ ] Redis 集群状态正常
- [ ] 配置中心配置正确
- [ ] 监控系统正常运行
- [ ] 日志收集正常
- [ ] 备份任务已配置
- [ ] 安全组规则已配置
- [ ] SSL 证书已配置

### B. 参考文档
- [Docker 官方文档](https://docs.docker.com/)
- [Kubernetes 官方文档](https://kubernetes.io/docs/)
- [Nacos 官方文档](https://nacos.io/)
- [SkyWalking 官方文档](https://skywalking.apache.org/)

---

**文档版本**: v1.0  
**最后更新**: 2026-05-20  
**维护者**: 筑基架构团队
