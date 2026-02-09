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
        maxWidth = "800px"
        style.set("margin", "0 auto")
        
        // 头部
        val header = HorizontalLayout(H2("在线客服")).apply {
            setWidthFull()
            addClassNames("bg-primary", "text-primary-contrast", "p-m")
        }
        
        // 消息区域
        messagesArea.apply {
            setWidthFull()
            addClassNames("bg-contrast-5", "p-m")
            style.set("flex", "1")
            style.set("overflow-y", "auto")
        }
        
        // 输入区域
        messageField.apply {
            placeholder = "输入消息..."
            setWidthFull()
        }
        
        val sendButton = Button("发送").apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addClickListener { sendMessage() }
        }
        
        messageField.addKeyPressListener(Key.ENTER, { _ -> sendMessage() })
        
        val inputLayout = HorizontalLayout(messageField, sendButton).apply {
            setWidthFull()
            addClassName("p-m")
            expand(messageField)
        }
        
        add(header, messagesArea, inputLayout)
        expand(messagesArea)
    }
    
    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            session = chatService.createGuestSession(ui)
            chatService.registerGuestRefresh(session.sessionId) { refreshMessages() }
            refreshMessages()
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
                    val bubble = Div(
                        Span(msg.from).apply { addClassName("text-xs") },
                        Div(msg.content),
                        Span(msg.timestamp.format(timeFormatter)).apply { addClassName("text-xs") }
                    ).apply {
                        addClassName("p-m")
                        style.set("border-radius", "var(--lumo-border-radius-m)")
                        style.set("max-width", "70%")
                        if (isAdmin) {
                            style.set("background-color", "white")
                            style.set("box-shadow", "var(--lumo-box-shadow-xs)")
                        } else {
                            addClassNames("bg-primary", "text-primary-contrast")
                        }
                    }
                    
                    val wrapper = HorizontalLayout(bubble).apply {
                        setWidthFull()
                        justifyContentMode = if (isAdmin) FlexComponent.JustifyContentMode.START else FlexComponent.JustifyContentMode.END
                    }
                    messagesArea.add(wrapper)
                }
            }
        }
    }
}
