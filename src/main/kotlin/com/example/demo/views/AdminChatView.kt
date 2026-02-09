package com.example.demo.views

import com.example.demo.service.ChatSession
import com.example.demo.service.CustomerChatService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

@Route("admin-chat")
class AdminChatView(@Autowired private val chatService: CustomerChatService) : KComposite() {
    
    private lateinit var userListContainer: Div
    private lateinit var chatContainer: Div
    private lateinit var messagesContainer: Div
    private lateinit var inputContainer: Div
    private var selectedSessionId: String? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    
    private val root = ui {
        horizontalLayout {
            setSizeFull()
            isPadding = false
            
            // 左侧用户列表
            verticalLayout {
                width = "300px"
                setHeightFull()
                isPadding = false
                style.set("border-right", "1px solid #ddd")
                
                // 头部
                div {
                    width = "100%"
                    style.set("padding", "15px")
                    style.set("background-color", "#1976d2")
                    style.set("color", "white")
                    style.set("font-weight", "bold")
                    text = "在线用户列表"
                }
                
                // 用户列表
                userListContainer = div {
                    width = "100%"
                    setHeightFull()
                    style.set("overflow-y", "auto")
                }
            }
            
            // 右侧聊天区域
            chatContainer = verticalLayout {
                setFlexGrow(1.0)
                setHeightFull()
                isPadding = false
                
                // 默认提示
                div {
                    setSizeFull()
                    style.set("display", "flex")
                    style.set("align-items", "center")
                    style.set("justify-content", "center")
                    style.set("color", "#999")
                    text = "请从左侧选择一个用户开始聊天"
                }
            }
        }
    }
    
    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            chatService.registerAdmin(ui)
            refreshUserList()
        }
    }
    
    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        ui.ifPresent { ui ->
            chatService.unregisterAdmin(ui)
        }
    }
    
    private fun refreshUserList() {
        ui.ifPresent { ui ->
            ui.access {
                userListContainer.removeAll()
                val sessions = chatService.getAllSessions()
                sessions.forEach { session ->
                    userListContainer.add(createUserItem(session))
                }
            }
        }
    }
    
    private fun createUserItem(session: ChatSession): Div {
        return Div().apply {
            width = "100%"
            style.set("padding", "15px")
            style.set("border-bottom", "1px solid #eee")
            style.set("cursor", "pointer")
            style.set("background-color", if (session.sessionId == selectedSessionId) "#e3f2fd" else "white")
            
            element.addEventListener("click") {
                selectSession(session.sessionId)
            }.addEventData("event.preventDefault()")
            
            // 用户名和在线状态
            add(Div().apply {
                style.set("display", "flex")
                style.set("align-items", "center")
                style.set("margin-bottom", "5px")
                
                add(Div().apply {
                    style.set("width", "10px")
                    style.set("height", "10px")
                    style.set("border-radius", "50%")
                    style.set("background-color", if (session.isOnline) "#4caf50" else "#999")
                    style.set("margin-right", "8px")
                })
                
                add(Div().apply {
                    style.set("font-weight", "bold")
                    text = session.guestName
                })
            })
            
            // 最后一条消息
            session.messages.lastOrNull()?.let { lastMsg ->
                add(Div().apply {
                    style.set("font-size", "12px")
                    style.set("color", "#666")
                    style.set("overflow", "hidden")
                    style.set("text-overflow", "ellipsis")
                    style.set("white-space", "nowrap")
                    text = "${lastMsg.from}: ${lastMsg.content}"
                })
            }
            
            // 未读消息数
            if (session.unreadCount > 0) {
                add(Div().apply {
                    style.set("position", "absolute")
                    style.set("right", "15px")
                    style.set("top", "15px")
                    style.set("background-color", "#f44336")
                    style.set("color", "white")
                    style.set("border-radius", "10px")
                    style.set("padding", "2px 8px")
                    style.set("font-size", "12px")
                    text = session.unreadCount.toString()
                })
            }
        }
    }
    
    private fun selectSession(sessionId: String) {
        selectedSessionId = sessionId
        chatService.clearUnreadCount(sessionId)
        refreshUserList()
        showChatArea(sessionId)
    }
    
    private fun showChatArea(sessionId: String) {
        val session = chatService.getSession(sessionId) ?: return
        
        chatContainer.removeAll()
        
        // 头部
        chatContainer.add(KComposite().apply {
            ui {
                horizontalLayout {
                    width = "100%"
                    isPadding = true
                    style.set("background-color", "#f5f5f5")
                    style.set("border-bottom", "1px solid #ddd")
                    
                    h3(session.guestName) {
                        style.set("margin", "0")
                    }
                    
                    span(if (session.isOnline) "在线" else "离线") {
                        style.set("color", if (session.isOnline) "#4caf50" else "#999")
                        style.set("margin-left", "10px")
                    }
                }
            }
        })
        
        // 消息区域
        messagesContainer = Div().apply {
            width = "100%"
            style.set("flex-grow", "1")
            style.set("overflow-y", "auto")
            style.set("padding", "20px")
            style.set("background-color", "#f5f5f5")
        }
        chatContainer.add(messagesContainer)
        refreshMessages(session)
        
        // 输入区域
        inputContainer = Div()
        chatContainer.add(KComposite().apply {
            ui {
                horizontalLayout {
                    width = "100%"
                    isPadding = true
                    style.set("background-color", "white")
                    style.set("border-top", "1px solid #ddd")
                    
                    val messageField = textField {
                        placeholder = "输入消息..."
                        width = "100%"
                    }
                    
                    button("发送") {
                        addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY)
                        onLeftClick {
                            val content = messageField.value.trim()
                            if (content.isNotEmpty()) {
                                chatService.sendAdminMessage(sessionId, content)
                                messageField.clear()
                                refreshMessages(session)
                                refreshUserList()
                            }
                        }
                    }
                    
                    messageField.addKeyPressListener { event ->
                        if (event.key.equals("Enter")) {
                            val content = messageField.value.trim()
                            if (content.isNotEmpty()) {
                                chatService.sendAdminMessage(sessionId, content)
                                messageField.clear()
                                refreshMessages(session)
                                refreshUserList()
                            }
                        }
                    }
                }
            }
        })
    }
    
    private fun refreshMessages(session: ChatSession) {
        ui.ifPresent { ui ->
            ui.access {
                messagesContainer.removeAll()
                session.messages.forEach { msg ->
                    messagesContainer.add(createMessageBubble(msg.from, msg.content, msg.timestamp.format(timeFormatter)))
                }
            }
        }
    }
    
    private fun createMessageBubble(from: String, content: String, time: String): Div {
        val isAdmin = from == "客服"
        return Div().apply {
            style.set("display", "flex")
            style.set("justify-content", if (isAdmin) "flex-end" else "flex-start")
            style.set("margin-bottom", "10px")
            
            val bubble = Div().apply {
                style.set("max-width", "70%")
                style.set("padding", "10px 15px")
                style.set("border-radius", "10px")
                style.set("background-color", if (isAdmin) "#c8e6c9" else "#e3f2fd")
                
                add(Div().apply {
                    style.set("font-weight", "bold")
                    style.set("font-size", "12px")
                    style.set("margin-bottom", "5px")
                    text = from
                })
                
                add(Div().apply {
                    text = content
                })
                
                add(Div().apply {
                    style.set("font-size", "10px")
                    style.set("color", "#666")
                    style.set("margin-top", "5px")
                    text = time
                })
            }
            add(bubble)
        }
    }
}
