package com.example.demo.service

import com.example.demo.entity.LogEntry
import com.example.demo.entity.LogLevel
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * 日志查询服务
 * 当前使用模拟数据，未来将连接到 Elasticsearch
 */
@Service
class LogQueryService {

    private val loggers = listOf(
        "com.example.demo.service.UserService",
        "com.example.demo.controller.ApiController",
        "com.example.demo.repository.DataRepository",
        "org.springframework.web.servlet.DispatcherServlet",
        "org.hibernate.SQL"
    )

    private val messages = listOf(
        "User login successful",
        "Database connection established",
        "API request received",
        "Processing payment transaction",
        "Cache miss for key: user_123",
        "Sending email notification",
        "File upload completed",
        "Session expired for user",
        "Invalid request parameter",
        "Database query executed in 45ms"
    )

    private val sources = listOf("app-server-01", "app-server-02", "worker-01", "worker-02")
    private val tags = listOf("production", "api", "database", "security", "performance")

    /**
     * 查询日志
     */
    fun queryLogs(
        keyword: String? = null,
        levels: Set<LogLevel>? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
        logger: String? = null,
        source: String? = null,
        limit: Int = 100
    ): List<LogEntry> {
        // 模拟数据生成
        return generateMockLogs(limit).filter { log ->
            (keyword == null || log.message.contains(keyword, ignoreCase = true) || 
                log.logger.contains(keyword, ignoreCase = true)) &&
            (levels == null || log.level in levels) &&
            (startTime == null || log.timestamp >= startTime) &&
            (endTime == null || log.timestamp <= endTime) &&
            (logger == null || log.logger.contains(logger, ignoreCase = true)) &&
            (source == null || log.source == source)
        }
    }

    /**
     * 获取日志统计信息
     */
    fun getLogStatistics(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Map<LogLevel, Long> {
        return LogLevel.entries.associateWith { Random.nextLong(10, 1000) }
    }

    /**
     * 获取可用的日志来源列表
     */
    fun getAvailableSources(): List<String> = sources

    /**
     * 获取可用的 Logger 列表
     */
    fun getAvailableLoggers(): List<String> = loggers

    private fun generateMockLogs(count: Int): List<LogEntry> {
        return (1..count).map { i ->
            val level = LogLevel.entries.random()
            LogEntry(
                id = "log-$i",
                timestamp = LocalDateTime.now().minusMinutes(Random.nextLong(0, 10000)),
                level = level,
                logger = loggers.random(),
                message = messages.random(),
                thread = "thread-${Random.nextInt(1, 10)}",
                exception = if (level == LogLevel.ERROR && Random.nextBoolean()) 
                    "java.lang.NullPointerException: Cannot invoke method on null object\n\tat com.example.Service.method(Service.java:123)" 
                    else null,
                source = sources.random(),
                tags = tags.shuffled().take(Random.nextInt(1, 3)),
                metadata = mapOf(
                    "userId" to "user-${Random.nextInt(1, 100)}",
                    "requestId" to "req-${Random.nextInt(1000, 9999)}"
                )
            )
        }.sortedByDescending { it.timestamp }
    }
}
