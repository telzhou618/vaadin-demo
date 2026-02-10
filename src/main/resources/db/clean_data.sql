-- 清理测试数据
USE chat_db;

-- 删除所有消息
TRUNCATE TABLE chat_message;

-- 删除所有会话
TRUNCATE TABLE chat_session;

-- 查看表结构
DESCRIBE chat_session;
DESCRIBE chat_message;
