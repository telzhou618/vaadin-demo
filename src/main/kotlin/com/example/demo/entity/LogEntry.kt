package com.example.demo.entity

import java.time.LocalDateTime

/**
 * 日志条目实体类
 * 未来将从 Elasticsearch 获取数据
 */
data class LogEntry(
    val id: String,
    val timestamp: LocalDateTime,
    val level: LogLevel,
    val logger: String,
    val message: String,
    val thread: String? = null,
    val exception: String? = null,
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

enum class LogLevel(val displayName: String, val color: String) {
    TRACE("TRACE", "#6c757d"),
    DEBUG("DEBUG", "#17a2b8"),
    INFO("INFO", "#28a745"),
    WARN("WARN", "#ffc107"),
    ERROR("ERROR", "#dc3545"),
    FATAL("FATAL", "#721c24")
}
