package com.example.demo.entity

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("chat_session")
data class ChatSessionEntity(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    
    @TableField("session_id")
    var sessionId: String,
    
    @TableField("guest_name")
    var guestName: String,
    
    @TableField("client_id")
    var clientId: String,
    
    @TableField("is_online")
    var isOnline: Boolean = false,
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    var createTime: LocalDateTime = LocalDateTime.now(),
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    var updateTime: LocalDateTime = LocalDateTime.now()
)
