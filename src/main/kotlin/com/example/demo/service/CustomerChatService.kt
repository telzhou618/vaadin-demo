package com.example.demo.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.example.demo.entity.ChatSessionEntity
import com.example.demo.mapper.ChatMessageMapper
import com.example.demo.mapper.ChatSessionMapper
import com.vaadin.flow.component.UI
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import com.example.demo.entity.ChatMessage as ChatMessageEntity

data class ChatMessage(
    val from: String,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ChatSession(
    val sessionId: String,
    val guestName: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    var guestUI: UI? = null,
    var isOnline: Boolean = true,
    var unreadCount: Int = 0
)

@Service
class CustomerChatService(
    private val chatMessageMapper: ChatMessageMapper,
    private val chatSessionMapper: ChatSessionMapper
) {
    private val sessions = ConcurrentHashMap<String, ChatSession>()
    private val adminUIs = ConcurrentHashMap.newKeySet<UI>()
    private val adminRefreshCallbacks = ConcurrentHashMap<UI, () -> Unit>()
    private val guestRefreshCallbacks = ConcurrentHashMap<String, () -> Unit>()
    private val sessionIdCounter = AtomicInteger(1)

    // 创建新的游客会话（使用客户端 ID）
    fun createGuestSession(guestUI: UI, clientId: String): ChatSession {
        // 如果已存在该客户端的会话，则恢复会话
        val existingSession = sessions[clientId]
        if (existingSession != null) {
            existingSession.guestUI = guestUI
            existingSession.isOnline = true
            updateSessionOnlineStatus(clientId, true)
            notifyAdmins()
            return existingSession
        }

        // 从数据库查找历史会话
        val dbSession = chatSessionMapper.selectOne(
            QueryWrapper<ChatSessionEntity>().eq("client_id", clientId)
        )

        if (dbSession != null) {
            // 恢复历史会话
            val messages = loadMessagesFromDB(dbSession.sessionId)
            val session = ChatSession(
                sessionId = dbSession.sessionId,
                guestName = dbSession.guestName,
                messages = messages,
                guestUI = guestUI,
                isOnline = true
            )
            sessions[clientId] = session
            updateSessionOnlineStatus(clientId, true)
            notifyAdmins()
            return session
        }

        // 创建新会话
        val guestName = "游客 ${clientId.takeLast(8)}"
        val session = ChatSession(clientId, guestName, guestUI = guestUI)
        sessions[clientId] = session

        // 保存会话到数据库
        val sessionEntity = ChatSessionEntity(
            sessionId = clientId,
            guestName = guestName,
            clientId = clientId,
            isOnline = true
        )
        chatSessionMapper.insert(sessionEntity)

        // 发送欢迎消息
        val welcomeMsg = ChatMessage("客服", "您好！欢迎咨询，请问有什么可以帮助您的？")
        session.messages.add(welcomeMsg)
        saveMessageToDB(clientId, welcomeMsg)

        // 通知所有管理员有新用户
        notifyAdmins()

        return session
    }

    // 游客发送消息
    fun sendGuestMessage(sessionId: String, content: String) {
        val session = sessions[sessionId] ?: return
        val message = ChatMessage(session.guestName, content)
        session.messages.add(message)
        session.unreadCount++

        // 保存到数据库
        saveMessageToDB(sessionId, message)

        // 通知管理员
        notifyAdmins()
    }

    // 客服发送消息
    fun sendAdminMessage(sessionId: String, content: String) {
        val session = sessions[sessionId] ?: return
        val message = ChatMessage("客服", content)
        session.messages.add(message)
        session.unreadCount = 0

        // 保存到数据库
        saveMessageToDB(sessionId, message)

        // 推送给游客
        session.guestUI?.access {
            guestRefreshCallbacks[sessionId]?.invoke()
            session.guestUI?.push()
        }

        // 通知其他管理员
        notifyAdmins()
    }

    // 注册管理员UI
    fun registerAdmin(ui: UI, refreshCallback: () -> Unit) {
        adminUIs.add(ui)
        adminRefreshCallbacks[ui] = refreshCallback

        // 首次加载时从数据库恢复所有会话
        if (sessions.isEmpty()) {
            loadAllSessionsFromDB()
        }
    }

    // 注销管理员UI
    fun unregisterAdmin(ui: UI) {
        adminUIs.remove(ui)
        adminRefreshCallbacks.remove(ui)
    }

    // 注册游客刷新回调
    fun registerGuestRefresh(sessionId: String, refreshCallback: () -> Unit) {
        guestRefreshCallbacks[sessionId] = refreshCallback
    }

    // 注销游客刷新回调
    fun unregisterGuestRefresh(sessionId: String) {
        guestRefreshCallbacks.remove(sessionId)
    }

    // 更新游客在线状态
    fun updateGuestStatus(sessionId: String, isOnline: Boolean) {
        sessions[sessionId]?.let {
            it.isOnline = isOnline
            if (!isOnline) {
                it.guestUI = null
            }
            updateSessionOnlineStatus(sessionId, isOnline)
            notifyAdmins()
        }
    }

    // 获取所有会话
    fun getAllSessions(): List<ChatSession> {
        return sessions.values.sortedByDescending { it.messages.lastOrNull()?.timestamp }
    }

    // 获取指定会话
    fun getSession(sessionId: String): ChatSession? {
        return sessions[sessionId]
    }

    // 清除未读数
    fun clearUnreadCount(sessionId: String) {
        sessions[sessionId]?.unreadCount = 0
        notifyAdmins()
    }

    // 通知所有管理员刷新
    private fun notifyAdmins() {
        adminUIs.forEach { ui ->
            ui.access {
                adminRefreshCallbacks[ui]?.invoke()
                ui.push()
            }
        }
    }

    // 保存消息到数据库
    private fun saveMessageToDB(sessionId: String, message: ChatMessage) {
        val entity = ChatMessageEntity(
            sessionId = sessionId,
            fromUser = message.from,
            content = message.content,
            createTime = message.timestamp
        )
        chatMessageMapper.insert(entity)
    }

    // 从数据库加载消息
    private fun loadMessagesFromDB(sessionId: String): MutableList<ChatMessage> {
        val entities = chatMessageMapper.selectList(
            QueryWrapper<ChatMessageEntity>()
                .eq("session_id", sessionId)
                .orderByAsc("create_time")
        )
        return entities.map {
            ChatMessage(it.fromUser, it.content, it.createTime)
        }.toMutableList()
    }

    // 更新会话在线状态
    private fun updateSessionOnlineStatus(sessionId: String, isOnline: Boolean) {
        chatSessionMapper.update(
            KtUpdateWrapper(ChatSessionEntity::class.java)
                .eq(ChatSessionEntity::sessionId, sessionId)
                .set(ChatSessionEntity::isOnline, isOnline)
                .set(ChatSessionEntity::updateTime, LocalDateTime.now())
        )
    }

    // 从数据库加载所有会话
    private fun loadAllSessionsFromDB() {
        val dbSessions = chatSessionMapper.selectList(null)
        dbSessions.forEach { dbSession ->
            if (!sessions.containsKey(dbSession.sessionId)) {
                val messages = loadMessagesFromDB(dbSession.sessionId)
                val session = ChatSession(
                    sessionId = dbSession.sessionId,
                    guestName = dbSession.guestName,
                    messages = messages,
                    isOnline = false
                )
                sessions[dbSession.sessionId] = session
            }
        }
    }
}
