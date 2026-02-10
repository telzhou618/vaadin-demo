package com.example.demo.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("chat_session")
data class ChatSessionEntity(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var sessionId: String,
    var guestName: String,
    var clientId: String,
    var isOnline: Boolean = false,
    var createTime: LocalDateTime = LocalDateTime.now(),
    var updateTime: LocalDateTime = LocalDateTime.now()
)
