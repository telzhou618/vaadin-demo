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
            style.set("max-width", "800px")
            style.set("margin", "0 auto")
            
            // 头部
            horizontalLayout {
                width = "100%"
                isPadding = true
                style.set("background-color", "#1976d2")
                style.set("color", "white")
                style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                
                h2("在线客服咨询") {
                    style.set("margin", "0")
                    style.set("font-size", "20px")
                }
            }
            
            // 消息区域
            messagesContainer = div {
                width = "100%"
                style.set("flex", "1")
                style.set("overflow-y", "auto")
                style.set("padding", "20px")
                style.set("background-color", "#f5f5f5")
            }
            
            // 输入区域
            horizontalLayout {
                width = "100%"
                isPadding = true
                style.set("background-color", "white")
                style.set("border-top", "2px solid #e0e0e0")
                style.set("gap", "10px")
                
                val messageField = textField {
                    placeholder = "输入消息..."
                    style.set("flex", "1")
                }
                
                button("发送") {
                    addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY)
                    style.set("min-width", "80px")
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
            chatService.registerGuestRefresh(session.sessionId) {
                refreshMessages()
            }
            refreshMessages()
        }
    }
    
    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        chatService.updateGuestStatus(session.sessionId, false)
        chatService.unregisterGuestRefresh(session.sessionId)
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
            style.set("margin-bottom", "15px")
            style.set("animation", "fadeIn 0.3s")
            
            val bubble = Div().apply {
                style.set("max-width", "60%")
                style.set("padding", "12px 16px")
                style.set("border-radius", "18px")
                style.set("background-color", if (isAdmin) "#ffffff" else "#1976d2")
                style.set("color", if (isAdmin) "#333" else "#ffffff")
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
