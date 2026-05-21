# 筑基 (Zhuji) 部署运维手册
# Zhuji Deployment and Operations Guide

## 目录 | Table of Contents
1. [部署架构 | Deployment Architecture](#1-部署架构--deployment-architecture)
2. [环境准备 | Environment Preparation](#2-环境准备--environment-preparation)
3. [Docker 部署 | Docker Deployment](#3-docker-部署--docker-deployment)
4. [Kubernetes 部署 | Kubernetes Deployment](#4-kubernetes-部署--kubernetes-deployment)
5. [配置管理 | Configuration Management](#5-配置管理--configuration-management)
6. [监控运维 | Monitoring and Operations](#6-监控运维--monitoring-and-operations)
7. [日志管理 | Log Management](#7-日志管理--log-management)
8. [备份恢复 | Backup and Recovery](#8-备份恢复--backup-and-recovery)
9. [故障排查 | Troubleshooting](#9-故障排查--troubleshooting)
10. [性能调优 | Performance Tuning](#10-性能调优--performance-tuning)

---

## 1. 部署架构 | Deployment Architecture

### 1.1 生产环境架构图 | Production Environment Architecture Diagram
```
┌─────────────────────────────────────────────────────────────────┐
│                          用户流量 | User Traffic                  │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                  负载均衡 (Nginx/SLB) | Load Balancer (Nginx/SLB)  │
│           SSL 终止 / 健康检查 / 负载分发 | SSL termination / Health check / Load distribution
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│         API 网关集群 (Spring Cloud Gateway) | API Gateway Cluster      │
│          2+ 实例，无状态，可水平扩展 | 2+ instances, stateless, horizontally scalable
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│          微服务集群 (Microservices) | Microservices Cluster            │
│  ┌──────────┬──────────┬──────────┬──────────┐                │
│  │用户组织   │ 多语言    │ API集成   │ 工作流   │                │
│  │服务      │ 服务      │ 服务      │ 服务     │                │
│  │(2实例)   │(2实例)   │(2实例)   │(2实例)  │                │
│  └──────────┴──────────┴──────────┴──────────┘                │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│               数据层 (Data Layer) | Data Layer                       │
│  ┌──────────┬──────────┬──────────┬──────────┐                │
│  │ MySQL    │  Redis   │ RabbitMQ │  Nacos   │                │
│  │(主从)    │ (Cluster)│ (Cluster)│ (Cluster)│                │
│  └──────────┴──────────┴──────────┴──────────┘                │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 高可用设计 | High Availability Design
| 组件 | Component | 高可用方案 | High Availability Solution | 说明 | Description |
|------|-----------|-----------|----------------------|------|-----------|
| API 网关 | API Gateway | 多实例 + 负载均衡 | Multiple instances + Load balancing | 无状态，可水平扩展 | Stateless, horizontally scalable |
| 微服务 | Microservices | 多实例 + 注册中心 | Multiple instances + Registry | 服务自动发现，故障自动摘除 | Automatic service discovery, automatic fault removal |
| MySQL | MySQL | 主从复制 + 读写分离 | Master-slave replication + Read-write separation | 主库故障自动切换从库 | Automatic failover to slave when master fails |
| Redis | Redis | Redis Cluster / Sentinel | Data sharding + Automatic failover | 数据分片 + 故障自动切换 | Data sharding + automatic failover |
| RabbitMQ | RabbitMQ | 镜像队列 + 集群 | Mirrored queues + Cluster | 队列数据多节点复制 | Queue data replicated across multiple nodes |
| Nacos | Nacos | 集群部署 + Raft | Cluster deployment + Raft | 数据一致性保证 | Data consistency guarantee |

---

## 2. 环境准备 | Environment Preparation

### 2.1 基础设施要求 | Infrastructure Requirements
| 资源 | Resource | 最低配置 | Minimum Configuration | 推荐配置 | Recommended Configuration | 说明 | Description |
|------|-----------|-----------|-------------------|------|-----------|
| CPU | CPU | 8 核 | 8 cores | 16 核 | 16 cores | 取决于并发量 | Depends on concurrency |
| 内存 | Memory | 16 GB | 32 GB | JVM + 中间件 | JVM + middleware |
| 磁盘 | Disk | 100 GB SSD | 500 GB SSD | 系统 + 日志 + 数据 | System + logs + data |
| 网络 | Network | 千兆网卡 | Gigabit NIC | 万兆网卡 | 10G NIC | 服务间通信 | Inter-service communication |
| 操作系统 | OS | Linux (CentOS 7+ / Ubuntu 20.04+) | Same as left | 稳定、安全 | Stable and secure |

### 2.2 软件环境 | Software Environment
| 软件 | Software | 版本 | Version | 说明 | Description |
|------|-----------|------|-----------|------|
| JDK | JDK | 17+ | LTS 版本 | LTS version |
| Docker | Docker | 24.x+ | 容器运行时 | Container runtime |
| Kubernetes | Kubernetes | 1.28+ | 容器编排 | Container orchestration |
| MySQL | MySQL | 8.0+ | 主数据库 | Primary database |
| Redis | Redis | 7.x+ | 缓存 + 分布式锁 | Cache + distributed lock |
| RabbitMQ | RabbitMQ | 3.12+ | 消息队列 | Message queue |
| Nacos | Nacos | 2.3+ | 服务注册 + 配置中心 | Service registry + config center |
| Nginx | Nginx | 1.24+ | 负载均衡 | Load balancing |

### 2.3 网络规划 | Network Planning
| 网络 | Network | CIDR | 说明 | Description |
|------|-----------|------|------|
| 公网 | Public | - | 用户访问 | User access |
| 负载均衡网络 | Load Balancer Network | 192.168.10.0/24 | 负载均衡器 | Load balancer |
| 应用网络 | Application Network | 192.168.20.0/24 | 应用服务器 | Application servers |
| 数据网络 | Data Network | 192.168.30.0/24 | 数据库、缓存 | Database, cache |
| 管理网络 | Management Network | 192.168.40.0/24 | 监控、日志 | Monitoring, logs |

---

## 3. Docker 部署 | Docker Deployment

### 3.1 构建镜像 | Build Images
**Maven 插件配置 | Maven Plugin Configuration** (`pom.xml`):
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

**构建命令 | Build Command**:
```bash
# 构建单个模块 | Build single module
mvn clean package dockerfile:build -DskipTests

# 构建所有模块 | Build all modules
mvn clean package dockerfile:build -DskipTests -pl user-org-service,workflow-service
```

### 3.2 Docker Compose 部署 | Docker Compose Deployment
**`docker-compose.yml`**:
```yaml
version: '3.8'

services:
    # MySQL | MySQL
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

    # Redis | Redis
    redis:
        image: redis:7-alpine
        container_name: zhuji-redis
        ports:
            - "6379:6379"
        volumes:
            - redis-data:/data
        networks:
            - zhuji-network

    # Nacos | Nacos
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

    # 用户组织服务 | User Organization Service
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

    # API 网关 | API Gateway
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

**启动命令 | Startup Command**:
```bash
# 启动所有服务 | Start all services
docker-compose up -d

# 查看日志 | View logs
docker-compose logs -f user-org-service

# 停止所有服务 | Stop all services
docker-compose down
```

---

## 4. Kubernetes 部署 | Kubernetes Deployment

### 4.1 命名空间 | Namespace
**`namespace.yaml`**:
```yaml
apiVersion: v1
kind: Namespace
metadata:
    name: zhuji-prod
```

### 4.2 ConfigMap (配置) | ConfigMap (Configuration)
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

### 4.3 Deployment (部署) | Deployment
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

### 4.4 Service (服务) | Service
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

### 4.5 Ingress (入口) | Ingress
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

### 4.6 部署命令 | Deployment Commands
```bash
# 创建命名空间 | Create namespace
kubectl apply -f namespace.yaml

# 创建 ConfigMap | Create ConfigMap
kubectl apply -f configmap.yaml

# 部署服务 | Deploy service
kubectl apply -f deployment.yaml

# 创建 Service | Create Service
kubectl apply -f service.yaml

# 创建 Ingress | Create Ingress
kubectl apply -f ingress.yaml

# 查看 Pod 状态 | View pod status
kubectl get pods -n zhuji-prod

# 查看日志 | View logs
kubectl logs -f <pod-name> -n zhuji-prod

# 扩容 | Scale
kubectl scale deployment user-org-service --replicas=3 -n zhuji-prod
```

---

## 5. 配置管理 | Configuration Management

### 5.1 Nacos 配置中心 | Nacos Config Center
**配置格式 | Configuration Format**:
```yaml
# 在 Nacos 控制台创建配置 | Create config in Nacos console
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

**Spring Boot 集成 | Spring Boot Integration**:
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

### 5.2 配置优先级 | Configuration Priority
1. 命令行参数 | Command line arguments (`--spring.datasource.url=...`)
2. Java 系统属性 | Java system properties (`-Dspring.datasource.url=...`)
3. 操作系统环境变量 | OS environment variables (`SPRING_DATASOURCE_URL`)
4. Nacos 配置中心 | Nacos config center
5. 应用内 `application.yml` | Application internal `application.yml`
6. 应用内 `application.properties` | Application internal `application.properties`

### 5.3 敏感配置加密 | Sensitive Configuration Encryption
**使用 Jasypt 加密 | Use Jasypt for Encryption**:
```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

**加密命令 | Encryption Command**:
```bash
java -cp jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
    input="mysecret" password="encryptionkey" algorithm=PBEWithMD5AndDES
```

**配置文件中使用 | Use in Config File**:
```yaml
spring:
    datasource:
        password: ENC(encrypted-string)
```

---

## 6. 监控运维 | Monitoring and Operations

### 6.1 Spring Boot Actuator
**依赖 | Dependency**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**配置 | Configuration**:
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
**Prometheus 配置 | Prometheus Configuration** (`prometheus.yml`):
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
- 导入社区 Dashboard | Import community Dashboard: [Spring Boot Dashboard](https://grafana.com/grafana/dashboards/10280)
- 自定义 Dashboard | Custom Dashboard: JVM、QPS、RT、错误率 | JVM, QPS, RT, Error rate

### 6.3 SkyWalking APM
**Agent 配置 | Agent Configuration**:
```bash
java -javaagent:/path/to/skywalking-agent.jar \
    -Dskywalking.agent.service_name=user-org-service \
    -Dskywalking.collector.backend_service=oap:11800 \
    -jar user-org-service.jar
```

**查看链路 | View Traces**:
- 访问 SkyWalking UI | Access SkyWalking UI: `http://skywalking-ui:8080`
- 查看服务拓扑、链路追踪、性能分析 | View service topology, distributed tracing, performance analysis

---

## 7. 日志管理 | Log Management

### 7.1 ELK 日志平台 | ELK Log Platform
**Logback 配置 | Logback Configuration** (`logback-spring.xml`):
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

**Logstash 配置 | Logstash Configuration** (`logstash.conf`):
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

### 7.2 日志规范 | Log Standards
**日志级别 | Log Levels**:
- `ERROR`: 错误日志，需立即处理 | Error log, needs immediate attention
- `WARN`: 警告日志，需关注 | Warning log, needs attention
- `INFO`: 信息日志，关键业务操作 | Info log, critical business operations
- `DEBUG`: 调试日志，排查问题 | Debug log, troubleshooting

**日志格式 | Log Format**:
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

## 8. 备份恢复 | Backup and Recovery

### 8.1 数据库备份 | Database Backup
**MySQL 备份脚本 | MySQL Backup Script** (`backup-mysql.sh`):
```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/data/backup/mysql"
mysqldump -h mysql -u root -p${MYSQL_ROOT_PASSWORD} --all-databases > ${BACKUP_DIR}/backup_${DATE}.sql
gzip ${BACKUP_DIR}/backup_${DATE}.sql

# 删除 7 天前的备份 | Delete backups older than 7 days
find ${BACKUP_DIR} -name "backup_*.sql.gz" -mtime +7 -delete
```

**定时任务 (crontab) | Scheduled Task (crontab)**:
```
0 2 * * * /opt/scripts/backup-mysql.sh >> /var/log/backup.log 2>&1
```

### 8.2 数据库恢复 | Database Recovery
**恢复命令 | Recovery Command**:
```bash
# 解压备份文件 | Uncompress backup file
gunzip backup_20260520_020000.sql.gz

# 恢复数据 | Restore data
mysql -h mysql -u root -p < backup_20260520_020000.sql
```

### 8.3 Redis 备份 | Redis Backup
**RDB 持久化 | RDB Persistence** (`redis.conf`):
```
save 900 1     # 900 秒内至少 1 次写操作 | At least 1 write in 900 seconds
save 300 10    # 300 秒内至少 10 次写操作 | At least 10 writes in 300 seconds
save 60 10000  # 60 秒内至少 10000 次写操作 | At least 10000 writes in 60 seconds
dir /data/redis
dbfilename dump.rdb
```

**AOF 持久化 | AOF Persistence**:
```
appendonly yes
appendfsync everysec
```

---

## 9. 故障排查 | Troubleshooting

### 9.1 常见问题 | Common Issues

**问题 1: 服务注册失败 | Issue 1: Service Registration Failed**
```
# 检查 Nacos 是否可用 | Check if Nacos is available
curl http://nacos:8848/nacos/actuator/health

# 检查网络连通性 | Check network connectivity
ping nacos
telnet nacos 8848

# 查看服务日志 | View service logs
kubectl logs -f <pod-name> -n zhuji-prod
```

**问题 2: 数据库连接池耗尽 | Issue 2: Database Connection Pool Exhausted**
```
# 查看数据库连接数 | View database connections
SHOW PROCESSLIST;

# 增加连接池配置 | Increase connection pool config
spring:
    datasource:
        hikari:
            maximum-pool-size: 50
            minimum-idle: 10
```

**问题 3: Redis 缓存雪崩 | Issue 3: Redis Cache Avalanche**
```
# 解决方案：缓存过期时间加上随机值 | Solution: Add random value to cache expiration
int expireTime = 3600 + new Random().nextInt(300); // 3600-3900 秒 | seconds
redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
```

### 9.2 排查工具 | Troubleshooting Tools

**JVM 问题排查 | JVM Troubleshooting**:
```bash
# 查看 JVM 参数 | View JVM parameters
jinfo -flags <pid>

# 查看堆内存使用 | View heap memory usage
jmap -heap <pid>

# 生成堆转储 | Generate heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# 查看线程栈 | View thread stack
jstack <pid> > thread.txt
```

**Arthas 在线诊断 | Arthas Online Diagnosis**:
```bash
# 启动 Arthas | Start Arthas
java -jar arthas-boot.jar

# 查看方法调用耗时 | View method invocation time
trace com.zhuji.UserService getUserById

# 查看 JVM 状态 | View JVM status
dashboard
```

---

## 10. 性能调优 | Performance Tuning

### 10.1 JVM 调优 | JVM Tuning
**推荐配置 | Recommended Configuration** (`jvm.options`):
```
-Xms2g              # 初始堆大小 | Initial heap size
-Xmx2g              # 最大堆大小 | Max heap size
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
-XX:+UseG1GC        # 使用 G1 垃圾收集器 | Use G1 garbage collector
-XX:MaxGCPauseMillis=200
-Xlog:gc*:file=/logs/gc.log:time,uptime,level,tags
```

### 10.2 数据库连接池调优 | Database Connection Pool Tuning
```yaml
spring:
    datasource:
        hikari:
            maximum-pool-size: 20     # 最大连接数 | Max connections
            minimum-idle: 5           # 最小空闲连接 | Min idle connections
            connection-timeout: 30000  # 连接超时时间（毫秒）| Connection timeout (ms)
            idle-timeout: 600000      # 空闲连接超时时间 | Idle timeout
            max-lifetime: 1800000     # 连接最大生命周期 | Max connection lifetime
```

### 10.3 Redis 调优 | Redis Tuning
```
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru    # 内存满时淘汰最近最少使用的键 | Evict least recently used keys when memory full
timeout 300                     # 客户端超时时间（秒）| Client timeout (seconds)
tcp-keepalive 60                # TCP 保活时间 | TCP keep alive time
```

### 10.4 Tomcat 调优 | Tomcat Tuning
```yaml
server:
    tomcat:
        threads:
            max: 200             # 最大线程数 | Max threads
            min-spare: 10       # 最小空闲线程 | Min idle threads
        connection-timeout: 5000 # 连接超时时间（毫秒）| Connection timeout (ms)
        keep-alive-timeout: 10000 # Keep-Alive 超时时间 | Keep-Alive timeout
```

---

## 附录 | Appendix

### A. 部署检查清单 | Deployment Checklist
- [ ] 所有服务已启动且健康 | All services started and healthy
- [ ] 数据库主从同步正常 | Database master-slave sync normal
- [ ] Redis 集群状态正常 | Redis cluster status normal
- [ ] 配置中心配置正确 | Config center config correct
- [ ] 监控系统正常运行 | Monitoring system running normally
- [ ] 日志收集正常 | Log collection normal
- [ ] 备份任务已配置 | Backup tasks configured
- [ ] 安全组规则已配置 | Security group rules configured
- [ ] SSL 证书已配置 | SSL certificate configured

### B. 参考文档 | References
- [Docker 官方文档 | Docker Official Documentation](https://docs.docker.com/)
- [Kubernetes 官方文档 | Kubernetes Official Documentation](https://kubernetes.io/docs/)
- [Nacos 官方文档 | Nacos Official Documentation](https://nacos.io/)
- [SkyWalking 官方文档 | SkyWalking Official Documentation](https://skywalking.apache.org/)

---

**文档版本 | Document Version**: v1.0  
**最后更新 | Last Updated**: 2026-05-20  
**维护者 | Maintainers**: 筑基架构团队 | Zhuji Architecture Team
