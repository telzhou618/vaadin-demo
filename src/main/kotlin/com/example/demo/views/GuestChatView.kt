package com.example.demo.views

import com.example.demo.components.EmojiPicker
import com.example.demo.service.ChatSession
import com.example.demo.service.CustomerChatService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

@Route("guest-chat")
class GuestChatView(@Autowired private val chatService: CustomerChatService) : KComposite() {

    private lateinit var session: ChatSession
    private lateinit var messagesArea: VerticalLayout
    private lateinit var messageField: TextField
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var lastMessageCount = 0

    private val root = ui {
        verticalLayout {
            setSizeFull()
            maxWidth = "900px"
            style.set("margin", "0 auto")
            isPadding = false

            // 头部
            horizontalLayout {
                setWidthFull()
                addClassNames("bg-primary", "text-primary-contrast", "p-l")
                style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                
                h2("💬 在线客服") {
                    style.set("margin", "0")
                }
            }

            // 消息区域
            messagesArea = verticalLayout {
                setWidthFull()
                addClassName("p-m")
                style.set("flex", "1")
                style.set("overflow-y", "auto")
                style.set("background", "linear-gradient(to bottom, #f5f7fa 0%, #e8ecf1 100%)")
            }

            // 输入区域
            horizontalLayout {
                setWidthFull()
                addClassName("p-m")
                style.set("background-color", "white")
                style.set("box-shadow", "0 -2px 4px rgba(0,0,0,0.05)")

                messageField = textField {
                    placeholder = "输入消息..."
                    setWidthFull()
                    style.set("border-radius", "20px")
                    addKeyPressListener(Key.ENTER, { sendMessage() })
                }

                button("😊") {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    style.set("border-radius", "50%")
                    style.set("min-width", "40px")
                    style.set("font-size", "20px")
                    onLeftClick {
                        ui.ifPresent { ui ->
                            EmojiPicker.show(ui, messageField, EmojiPicker.Position.CENTER)
                        }
                    }
                }

                button("发送") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    style.set("border-radius", "20px")
                    onLeftClick { sendMessage() }
                }

                this@verticalLayout.expand(messagesArea)
            }
        }
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            initTitleBlink(ui)
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
                lastMessageCount = session.messages.size
            }
        }
    }

    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        if (::session.isInitialized) {
            chatService.updateGuestStatus(session.sessionId, false)
            chatService.unregisterGuestRefresh(session.sessionId)
        }
    }

    private fun sendMessage() {
        val content = messageField.value?.trim() ?: ""
        if (content.isNotEmpty()) {
            chatService.sendGuestMessage(session.sessionId, content)
            messageField.clear()
            refreshMessages()
            stopTitleBlink()
        }
    }

    private fun initTitleBlink(ui: UI) {
        ui.page.executeJs(
            """
            window.originalTitle = document.title;
            window.titleBlinkInterval = null;
            window.isPageVisible = true;
            
            document.addEventListener('visibilitychange', () => {
                window.isPageVisible = !document.hidden;
                if (window.isPageVisible && window.titleBlinkInterval) {
                    clearInterval(window.titleBlinkInterval);
                    window.titleBlinkInterval = null;
                    document.title = window.originalTitle;
                }
            });
            
            window.addEventListener('focus', () => {
                window.isPageVisible = true;
                if (window.titleBlinkInterval) {
                    clearInterval(window.titleBlinkInterval);
                    window.titleBlinkInterval = null;
                    document.title = window.originalTitle;
                }
            });
            
            window.startTitleBlink = () => {
                if (!window.isPageVisible && !window.titleBlinkInterval) {
                    let toggle = false;
                    window.titleBlinkInterval = setInterval(() => {
                        document.title = toggle ? window.originalTitle : '💬 新消息！';
                        toggle = !toggle;
                    }, 1000);
                }
            };
            
            window.stopTitleBlink = () => {
                if (window.titleBlinkInterval) {
                    clearInterval(window.titleBlinkInterval);
                    window.titleBlinkInterval = null;
                    document.title = window.originalTitle;
                }
            };
            """
        )
    }

    private fun startTitleBlink() {
        ui.ifPresent { it.page.executeJs("if (window.startTitleBlink) window.startTitleBlink();") }
    }

    private fun stopTitleBlink() {
        ui.ifPresent { it.page.executeJs("if (window.stopTitleBlink) window.stopTitleBlink();") }
    }

    private fun refreshMessages() {
        ui.ifPresent { ui ->
            ui.access {
                val currentMessageCount = session.messages.size
                val hasNewAdminMessage = currentMessageCount > lastMessageCount &&
                        session.messages.lastOrNull()?.from == "客服"

                messagesArea.removeAll()
                session.messages.forEach { msg ->
                    val isAdmin = msg.from == "客服"
                    
                    messagesArea.horizontalLayout {
                        setWidthFull()
                        justifyContentMode = if (isAdmin) FlexComponent.JustifyContentMode.START 
                                            else FlexComponent.JustifyContentMode.END

                        div {
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

                            span(msg.from) {
                                addClassName("text-xs")
                                style.set("font-weight", "600")
                                style.set("display", "block")
                                style.set("margin-bottom", "4px")
                                if (!isAdmin) style.set("opacity", "0.9")
                            }

                            div {
                                text = msg.content
                                style.set("line-height", "1.5")
                                style.set("margin-bottom", "4px")
                            }

                            span(msg.timestamp.format(timeFormatter)) {
                                addClassName("text-xs")
                                style.set("opacity", "0.7")
                                style.set("display", "block")
                                style.set("text-align", "right")
                            }
                        }
                    }
                }

                messagesArea.element.executeJs("this.scrollTop = this.scrollHeight")

                if (hasNewAdminMessage) {
                    startTitleBlink()
                }

                lastMessageCount = currentMessageCount
            }
        }
    }
}
