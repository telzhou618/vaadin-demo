package com.example.demo.views

import com.example.demo.service.ChatSession
import com.example.demo.service.CustomerChatService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

@Route("guest-chat")
class GuestChatView(@Autowired private val chatService: CustomerChatService) : KComposite() {
    
    private lateinit var session: ChatSession
    private lateinit var messagesContainer: Div
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    
    private val root = ui {
        verticalLayout {
            setSizeFull()
            isPadding = false
            
            // 头部
            horizontalLayout {
                width = "100%"
                isPadding = true
                style.set("background-color", "#1976d2")
                style.set("color", "white")
                
                h2("在线客服咨询") {
                    style.set("margin", "0")
                }
            }
            
            // 消息区域
            messagesContainer = div {
                width = "100%"
                setHeightFull()
                style.set("overflow-y", "auto")
                style.set("padding", "20px")
                style.set("background-color", "#f5f5f5")
            }
            
            // 输入区域
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
                            chatService.sendGuestMessage(session.sessionId, content)
                            messageField.clear()
                            refreshMessages()
                        }
                    }
                }
                
                messageField.addKeyPressListener { event ->
                    if (event.key.equals("Enter")) {
                        val content = messageField.value.trim()
                        if (content.isNotEmpty()) {
                            chatService.sendGuestMessage(session.sessionId, content)
                            messageField.clear()
                            refreshMessages()
                        }
                    }
                }
            }
        }
    }
    
    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            session = chatService.createGuestSession(ui)
            refreshMessages()
        }
    }
    
    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        chatService.updateGuestStatus(session.sessionId, false)
    }
    
    private fun refreshMessages() {
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
            style.set("justify-content", if (isAdmin) "flex-start" else "flex-end")
            style.set("margin-bottom", "10px")
            
            val bubble = Div().apply {
                style.set("max-width", "70%")
                style.set("padding", "10px 15px")
                style.set("border-radius", "10px")
                style.set("background-color", if (isAdmin) "#e3f2fd" else "#c8e6c9")
                
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
