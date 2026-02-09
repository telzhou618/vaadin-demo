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
    private lateinit var chatContainer: com.vaadin.flow.component.orderedlayout.VerticalLayout
    private lateinit var messagesContainer: Div
    private lateinit var inputContainer: Div
    private var selectedSessionId: String? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val root = ui {
        horizontalLayout {
            setSizeFull()
            isPadding = false
            style.set("gap", "0")

            // 左侧用户列表
            verticalLayout {
                width = "320px"
                setHeightFull()
                isPadding = false
                style.set("border-right", "2px solid #e0e0e0")
                style.set("background-color", "#fafafa")

                // 头部
                div {
                    width = "100%"
                    style.set("padding", "20px")
                    style.set("background-color", "#1976d2")
                    style.set("color", "white")
                    style.set("font-weight", "600")
                    style.set("font-size", "18px")
                    style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                    text = "客服管理端"
                }

                // 用户列表
                userListContainer = div {
                    width = "100%"
                    style.set("flex", "1")
                    style.set("overflow-y", "auto")
                    style.set("background-color", "#ffffff")
                }
            }

            // 右侧聊天区域
            chatContainer = verticalLayout {
                setFlexGrow(1.0)
                setHeightFull()
                isPadding = false
                style.set("background-color", "#f5f5f5")

                // 默认提示
                div {
                    setSizeFull()
                    style.set("display", "flex")
                    style.set("align-items", "center")
                    style.set("justify-content", "center")
                    style.set("color", "#999")
                    style.set("font-size", "16px")
                    text = "👈 请从左侧选择一个用户开始聊天"
                }
            }
        }
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            chatService.registerAdmin(ui) {
                refreshUserList()
                // 如果当前选中了某个会话，也刷新消息
                selectedSessionId?.let { sessionId ->
                    chatService.getSession(sessionId)?.let { session ->
                        refreshMessages(session)
                    }
                }
            }
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
            style.set("padding", "16px")
            style.set("border-bottom", "1px solid #f0f0f0")
            style.set("cursor", "pointer")
            style.set("position", "relative")
            style.set("transition", "background-color 0.2s")
            style.set("background-color", if (session.sessionId == selectedSessionId) "#e3f2fd" else "white")

            element.addEventListener("click") {
                selectSession(session.sessionId)
            }.addEventData("event.preventDefault()")
            
            element.addEventListener("mouseenter") {
                if (session.sessionId != selectedSessionId) {
                    style.set("background-color", "#f5f5f5")
                }
            }.addEventData("event.preventDefault()")
            
            element.addEventListener("mouseleave") {
                if (session.sessionId != selectedSessionId) {
                    style.set("background-color", "white")
                }
            }.addEventData("event.preventDefault()")

            // 用户名和在线状态
            add(Div().apply {
                style.set("display", "flex")
                style.set("align-items", "center")
                style.set("margin-bottom", "6px")

                add(Div().apply {
                    style.set("width", "8px")
                    style.set("height", "8px")
                    style.set("border-radius", "50%")
                    style.set("background-color", if (session.isOnline) "#4caf50" else "#bdbdbd")
                    style.set("margin-right", "10px")
                    style.set("flex-shrink", "0")
                })

                add(Div().apply {
                    style.set("font-weight", "600")
                    style.set("font-size", "14px")
                    style.set("color", "#333")
                    text = session.guestName
                })
            })

            // 最后一条消息
            session.messages.lastOrNull()?.let { lastMsg ->
                add(Div().apply {
                    style.set("font-size", "13px")
                    style.set("color", "#757575")
                    style.set("overflow", "hidden")
                    style.set("text-overflow", "ellipsis")
                    style.set("white-space", "nowrap")
                    style.set("padding-left", "18px")
                    text = "${lastMsg.from}: ${lastMsg.content}"
                })
            }

            // 未读消息数
            if (session.unreadCount > 0) {
                add(Div().apply {
                    style.set("position", "absolute")
                    style.set("right", "16px")
                    style.set("top", "16px")
                    style.set("background-color", "#f44336")
                    style.set("color", "white")
                    style.set("border-radius", "12px")
                    style.set("padding", "4px 8px")
                    style.set("font-size", "11px")
                    style.set("font-weight", "600")
                    style.set("min-width", "20px")
                    style.set("text-align", "center")
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
        val headerLayout = com.vaadin.flow.component.orderedlayout.HorizontalLayout().apply {
            width = "100%"
            isPadding = true
            style.set("background-color", "#ffffff")
            style.set("border-bottom", "2px solid #e0e0e0")
            style.set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)")

            add(com.vaadin.flow.component.html.H3(session.guestName).apply {
                style.set("margin", "0")
                style.set("font-size", "18px")
                style.set("color", "#333")
            })

            add(com.vaadin.flow.component.html.Span(if (session.isOnline) "● 在线" else "○ 离线").apply {
                style.set("color", if (session.isOnline) "#4caf50" else "#bdbdbd")
                style.set("margin-left", "12px")
                style.set("font-size", "14px")
            })
        }
        chatContainer.add(headerLayout)

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
        val inputLayout = com.vaadin.flow.component.orderedlayout.HorizontalLayout().apply {
            width = "100%"
            isPadding = true
            style.set("background-color", "white")
            style.set("border-top", "2px solid #e0e0e0")
            style.set("gap", "10px")

            val messageField = com.vaadin.flow.component.textfield.TextField().apply {
                placeholder = "输入消息..."
                style.set("flex", "1")
            }
            add(messageField)

            val sendButton = com.vaadin.flow.component.button.Button("发送").apply {
                addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY)
                style.set("min-width", "80px")
                addClickListener {
                    val content = messageField.value.trim()
                    if (content.isNotEmpty()) {
                        chatService.sendAdminMessage(sessionId, content)
                        messageField.clear()
                        refreshMessages(session)
                        refreshUserList()
                    }
                }
            }
            add(sendButton)

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
        chatContainer.add(inputLayout)
    }

    private fun refreshMessages(session: ChatSession) {
        ui.ifPresent { ui ->
            ui.access {
                messagesContainer.removeAll()
                session.messages.forEach { msg ->
                    messagesContainer.add(
                        createMessageBubble(
                            msg.from,
                            msg.content,
                            msg.timestamp.format(timeFormatter)
                        )
                    )
                }
            }
        }
    }

    private fun createMessageBubble(from: String, content: String, time: String): Div {
        val isAdmin = from == "客服"
        return Div().apply {
            style.set("display", "flex")
            style.set("justify-content", if (isAdmin) "flex-end" else "flex-start")
            style.set("margin-bottom", "15px")

            val bubble = Div().apply {
                style.set("max-width", "60%")
                style.set("padding", "12px 16px")
                style.set("border-radius", "18px")
                style.set("background-color", if (isAdmin) "#1976d2" else "#ffffff")
                style.set("color", if (isAdmin) "#ffffff" else "#333")
                style.set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")

                add(Div().apply {
                    style.set("font-weight", "600")
                    style.set("font-size", "11px")
                    style.set("margin-bottom", "4px")
                    style.set("opacity", "0.8")
                    text = from
                })

                add(Div().apply {
                    style.set("font-size", "14px")
                    style.set("line-height", "1.4")
                    style.set("word-wrap", "break-word")
                    text = content
                })

                add(Div().apply {
                    style.set("font-size", "10px")
                    style.set("opacity", "0.6")
                    style.set("margin-top", "4px")
                    style.set("text-align", "right")
                    text = time
                })
            }
            add(bubble)
        }
    }
}
