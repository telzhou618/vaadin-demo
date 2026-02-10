package com.example.demo.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.example.demo.entity.ChatMessage
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ChatMessageMapper : BaseMapper<ChatMessage>
