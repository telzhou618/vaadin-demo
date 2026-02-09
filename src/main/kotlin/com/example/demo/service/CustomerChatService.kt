package com.example.demo.service

import com.vaadin.flow.component.UI
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

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
class CustomerChatService {
    private val sessions = ConcurrentHashMap<String, ChatSession>()
    private val adminUIs = ConcurrentHashMap.newKeySet<UI>()
    private val adminRefreshCallbacks = ConcurrentHashMap<UI, () -> Unit>()
    private val guestRefreshCallbacks = ConcurrentHashMap<String, () -> Unit>()
    private val sessionIdCounter = AtomicInteger(1)
    
    // 创建新的游客会话
    fun createGuestSession(guestUI: UI): ChatSession {
        val sessionId = "GUEST-${sessionIdCounter.getAndIncrement()}"
        val guestName = "游客 $sessionId"
        val session = ChatSession(sessionId, guestName, guestUI = guestUI)
        sessions[sessionId] = session
        
        // 发送欢迎消息
        val welcomeMsg = ChatMessage("客服", "您好！欢迎咨询，请问有什么可以帮助您的？")
        session.messages.add(welcomeMsg)
        
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
        
        // 通知管理员
        notifyAdmins()
    }
    
    // 客服发送消息
    fun sendAdminMessage(sessionId: String, content: String) {
        val session = sessions[sessionId] ?: return
        val message = ChatMessage("客服", content)
        session.messages.add(message)
        session.unreadCount = 0
        
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
}
