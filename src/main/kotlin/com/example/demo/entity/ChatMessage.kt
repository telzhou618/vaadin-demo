package com.example.demo.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("chat_message")
data class ChatMessage(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var sessionId: String,
    var fromUser: String,
    var content: String,
    var createTime: LocalDateTime = LocalDateTime.now()
)
