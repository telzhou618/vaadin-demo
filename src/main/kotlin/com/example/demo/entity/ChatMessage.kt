package com.example.demo.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("chat_message")
data class ChatMessage(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    
    @TableField("session_id")
    var sessionId: String,
    
    @TableField("from_user")
    var fromUser: String,
    
    @TableField("content")
    var content: String,
    
    @TableField("create_time")
    var createTime: LocalDateTime = LocalDateTime.now()
)
