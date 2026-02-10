# 数据库配置说明

## 1. 安装 MySQL

确保已安装 MySQL 8.0 或更高版本。

## 2. 创建数据库

执行以下 SQL 脚本创建数据库和表：

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

或者手动执行 `src/main/resources/db/schema.sql` 中的 SQL 语句。

## 3. 配置数据库连接

修改 `src/main/resources/application.yaml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chat_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root        # 修改为你的数据库用户名
    password: root        # 修改为你的数据库密码
```

## 4. 功能说明

### 持久化功能

- **会话持久化**：所有聊天会话保存到 `chat_session` 表
- **消息持久化**：所有聊天消息保存到 `chat_message` 表
- **历史恢复**：客户端重新连接时自动恢复历史会话和消息
- **在线状态**：实时更新并保存用户在线状态

### 数据表结构

#### chat_session（聊天会话表）
- `id`: 主键
- `session_id`: 会话ID（唯一）
- `guest_name`: 访客名称
- `client_id`: 客户端ID
- `is_online`: 是否在线
- `create_time`: 创建时间
- `update_time`: 更新时间

#### chat_message（聊天消息表）
- `id`: 主键
- `session_id`: 会话ID
- `from_user`: 发送者
- `content`: 消息内容
- `create_time`: 创建时间

## 5. 启动应用

```bash
./gradlew bootRun
```

应用启动后会自动连接数据库，所有聊天记录将被持久化保存。
