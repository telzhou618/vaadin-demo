package com.example.demo.views

import com.example.demo.service.ChatSession
import com.example.demo.service.CustomerChatService
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

@Route("guest-chat")
class GuestChatView(@Autowired private val chatService: CustomerChatService) : VerticalLayout() {
    
    private lateinit var session: ChatSession
    private val messagesArea = VerticalLayout()
    private val messageField = TextField()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    init {
        setSizeFull()
        maxWidth = "900px"
        style.set("margin", "0 auto")
        isPadding = false
        
        // 头部
        val header = HorizontalLayout(H2("💬 在线客服").apply {
            style.set("margin", "0")
        }).apply {
            setWidthFull()
            addClassNames("bg-primary", "text-primary-contrast", "p-l")
            style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
        }
        
        // 消息区域
        messagesArea.apply {
            setWidthFull()
            addClassName("p-m")
            style.set("flex", "1")
            style.set("overflow-y", "auto")
            style.set("background", "linear-gradient(to bottom, #f5f7fa 0%, #e8ecf1 100%)")
        }
        
        // 输入区域
        messageField.apply {
            placeholder = "输入消息..."
            setWidthFull()
            style.set("border-radius", "20px")
        }
        
        val sendButton = Button("发送").apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            style.set("border-radius", "20px")
            addClickListener { sendMessage() }
        }
        
        messageField.addKeyPressListener(Key.ENTER, { _ -> sendMessage() })
        
        val inputLayout = HorizontalLayout(messageField, sendButton).apply {
            setWidthFull()
            addClassName("p-m")
            style.set("background-color", "white")
            style.set("box-shadow", "0 -2px 4px rgba(0,0,0,0.05)")
            expand(messageField)
        }
        
        add(header, messagesArea, inputLayout)
        expand(messagesArea)
    }
    
    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            // 从浏览器 localStorage 获取或生成客户端 ID
            ui.page.executeJs(
                """
                let clientId = localStorage.getItem('guestClientId');
                if (!clientId) {
                    clientId = 'GUEST-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
                    localStorage.setItem('guestClientId', clientId);
                }
                return clientId;
                """
            ).then { result ->
                val clientId = result.asString()
                session = chatService.createGuestSession(ui, clientId)
                chatService.registerGuestRefresh(session.sessionId) { refreshMessages() }
                refreshMessages()
            }
        }
    }
    
    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        chatService.updateGuestStatus(session.sessionId, false)
        chatService.unregisterGuestRefresh(session.sessionId)
    }
    
    private fun sendMessage() {
        val content = messageField.value?.trim() ?: ""
        if (content.isNotEmpty()) {
            chatService.sendGuestMessage(session.sessionId, content)
            messageField.clear()
            refreshMessages()
        }
    }
    
    private fun refreshMessages() {
        ui.ifPresent { ui ->
            ui.access {
                messagesArea.removeAll()
                session.messages.forEach { msg ->
                    val isAdmin = msg.from == "客服"
                    val bubble = Div().apply {
                        addClassName("p-m")
                        style.set("border-radius", "12px")
                        style.set("max-width", "70%")
                        style.set("word-wrap", "break-word")
                        
                        if (isAdmin) {
                            style.set("background-color", "white")
                            style.set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)")
                        } else {
                            addClassNames("bg-primary", "text-primary-contrast")
                            style.set("box-shadow", "0 1px 3px rgba(0,0,0,0.2)")
                        }
                        
                        // 发送者名称
                        add(Span(msg.from).apply {
                            addClassName("text-xs")
                            style.set("font-weight", "600")
                            style.set("display", "block")
                            style.set("margin-bottom", "4px")
                            if (!isAdmin) {
                                style.set("opacity", "0.9")
                            }
                        })
                        
                        // 消息内容
                        add(Div(msg.content).apply {
                            style.set("line-height", "1.5")
                            style.set("margin-bottom", "4px")
                        })
                        
                        // 时间戳
                        add(Span(msg.timestamp.format(timeFormatter)).apply {
                            addClassName("text-xs")
                            style.set("opacity", "0.7")
                            style.set("display", "block")
                            style.set("text-align", "right")
                        })
                    }
                    
                    val wrapper = HorizontalLayout(bubble).apply {
                        setWidthFull()
                        justifyContentMode = if (isAdmin) FlexComponent.JustifyContentMode.START else FlexComponent.JustifyContentMode.END
                    }
                    messagesArea.add(wrapper)
                }
                
                // 滚动到底部
                messagesArea.element.executeJs("this.scrollTop = this.scrollHeight")
            }
        }
    }
}
