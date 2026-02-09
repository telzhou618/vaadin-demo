package com.example.demo.views

import com.example.demo.service.ChatSession
import com.example.demo.service.CustomerChatService
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

@Route("admin-chat")
class AdminChatView(@Autowired private val chatService: CustomerChatService) : HorizontalLayout() {
    
    private val userList = VerticalLayout()
    private val chatArea = VerticalLayout()
    private val messagesArea = VerticalLayout()
    private var selectedSessionId: String? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    init {
        setSizeFull()
        
        // 左侧用户列表
        val leftPanel = VerticalLayout().apply {
            width = "300px"
            setHeightFull()
            isPadding = false
            
            add(HorizontalLayout(H3("客服管理端")).apply {
                setWidthFull()
                addClassNames("bg-primary", "text-primary-contrast", "p-m")
            })
            
            userList.apply {
                setWidthFull()
                style.set("overflow-y", "auto")
            }
            add(userList)
            expand(userList)
        }
        
        // 右侧聊天区域
        chatArea.apply {
            setHeightFull()
            isPadding = false
            addClassName("bg-contrast-5")
            
            add(Div(Span("👈 请选择用户开始聊天").apply {
                addClassName("text-secondary")
            }).apply {
                setSizeFull()
                style.set("display", "flex")
                style.set("align-items", "center")
                style.set("justify-content", "center")
            })
        }
        
        add(leftPanel, chatArea)
        expand(chatArea)
    }
    
    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            chatService.registerAdmin(ui) {
                refreshUserList()
                selectedSessionId?.let { sessionId ->
                    chatService.getSession(sessionId)?.let { refreshMessages(it) }
                }
            }
            refreshUserList()
        }
    }
    
    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        ui.ifPresent { ui -> chatService.unregisterAdmin(ui) }
    }
    
    private fun refreshUserList() {
        ui.ifPresent { ui ->
            ui.access {
                userList.removeAll()
                chatService.getAllSessions().forEach { session ->
                    val userItem = createUserItem(session)
                    userList.add(userItem)
                }
            }
        }
    }
    
    private fun createUserItem(session: ChatSession): HorizontalLayout {
        val statusIcon = Icon(if (session.isOnline) VaadinIcon.CIRCLE else VaadinIcon.CIRCLE_THIN).apply {
            style.set("width", "8px")
            style.set("height", "8px")
            if (session.isOnline) addClassName("text-success") else addClassName("text-disabled")
        }
        
        val nameLabel = Span(session.guestName).apply {
            style.set("font-weight", "600")
        }
        
        return HorizontalLayout().apply {
            setWidthFull()
            addClassName("p-m")
            style.set("cursor", "pointer")
            if (session.sessionId == selectedSessionId) {
                addClassName("bg-primary-10pct")
            }
            
            add(statusIcon, nameLabel)
            
            if (session.unreadCount > 0) {
                add(Span(session.unreadCount.toString()).apply {
                    style.set("background", "var(--lumo-error-color)")
                    style.set("color", "white")
                    style.set("border-radius", "10px")
                    style.set("padding", "2px 6px")
                    style.set("font-size", "11px")
                    style.set("margin-left", "auto")
                })
            }
            
            element.addEventListener("click") {
                selectSession(session.sessionId)
            }.addEventData("event.preventDefault()")
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
        
        chatArea.removeAll()
        
        // 头部
        val header = HorizontalLayout(
            H3(session.guestName),
            Span(if (session.isOnline) "● 在线" else "○ 离线").apply {
                addClassName(if (session.isOnline) "text-success" else "text-disabled")
            }
        ).apply {
            setWidthFull()
            addClassName("p-m")
        }
        
        // 消息区域
        messagesArea.apply {
            setWidthFull()
            addClassName("p-m")
            style.set("overflow-y", "auto")
        }
        
        // 输入区域
        val messageField = TextField().apply {
            placeholder = "输入消息..."
            setWidthFull()
        }
        
        val sendButton = Button("发送").apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addClickListener {
                val content = messageField.value?.trim() ?: ""
                if (content.isNotEmpty()) {
                    chatService.sendAdminMessage(sessionId, content)
                    messageField.clear()
                    refreshMessages(session)
                    refreshUserList()
                }
            }
        }
        
        messageField.addKeyPressListener(Key.ENTER, { _ ->
            val content = messageField.value?.trim() ?: ""
            if (content.isNotEmpty()) {
                chatService.sendAdminMessage(sessionId, content)
                messageField.clear()
                refreshMessages(session)
                refreshUserList()
            }
        })
        
        val inputLayout = HorizontalLayout(messageField, sendButton).apply {
            setWidthFull()
            addClassName("p-m")
            expand(messageField)
        }
        
        chatArea.add(header, messagesArea, inputLayout)
        chatArea.expand(messagesArea)
        
        refreshMessages(session)
    }
    
    private fun refreshMessages(session: ChatSession) {
        ui.ifPresent { ui ->
            ui.access {
                messagesArea.removeAll()
                session.messages.forEach { msg ->
                    val isAdmin = msg.from == "客服"
                    val bubble = Div(
                        Span(msg.from).apply { addClassName("text-xs") },
                        Div(msg.content),
                        Span(msg.timestamp.format(timeFormatter)).apply { addClassName("text-xs") }
                    ).apply {
                        addClassName("p-m")
                        style.set("border-radius", "var(--lumo-border-radius-m)")
                        style.set("max-width", "70%")
                        if (isAdmin) {
                            addClassNames("bg-primary", "text-primary-contrast")
                        } else {
                            style.set("background-color", "white")
                            style.set("box-shadow", "var(--lumo-box-shadow-xs)")
                        }
                    }
                    
                    val wrapper = HorizontalLayout(bubble).apply {
                        setWidthFull()
                        justifyContentMode = if (isAdmin) FlexComponent.JustifyContentMode.END else FlexComponent.JustifyContentMode.START
                    }
                    messagesArea.add(wrapper)
                }
                
                // 滚动到底部
                messagesArea.element.executeJs("this.scrollTop = this.scrollHeight")
            }
        }
    }
}
