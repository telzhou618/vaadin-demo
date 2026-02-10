package com.example.demo.views

import com.example.demo.components.EmojiPicker
import com.example.demo.service.ChatSession
import com.example.demo.service.CustomerChatService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Span
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
    private lateinit var messagesArea: VerticalLayout
    private var selectedSessionId: String? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var headerStatusSpan: Span? = null

    init {
        setSizeFull()
        isPadding = false
        isSpacing = false

        // 左侧用户列表面板
        val leftPanel = verticalLayout {
            width = "320px"
            setHeightFull()
            isPadding = false
            isSpacing = false
            style.set("border-right", "1px solid var(--lumo-contrast-10pct)")

            // 标题栏
            horizontalLayout {
                setWidthFull()
                addClassNames("bg-primary", "text-primary-contrast", "p-l")
                style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                alignItems = FlexComponent.Alignment.CENTER
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

                icon(VaadinIcon.USERS) {
                    style.set("margin-right", "8px")
                }
                h3("客服管理端") {
                    style.set("margin", "0")
                }
            }

            // 用户列表
            userList.apply {
                setWidthFull()
                isPadding = false
                isSpacing = false
                style.set("overflow-y", "auto")
                style.set("background-color", "white")
            }

            add(userList)
            expand(userList)
        }

        // 右侧聊天区域
        chatArea.apply {
            setHeightFull()
            isPadding = false
            isSpacing = false
            style.set("background", "linear-gradient(to bottom, #f5f7fa 0%, #e8ecf1 100%)")
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER

            span("👈 请选择用户开始聊天") {
                addClassName("text-secondary")
                style.set("font-size", "18px")
            }
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
                    userList.add(createUserItem(session))
                }

                // 同步更新聊天区域头部的在线状态
                selectedSessionId?.let { sessionId ->
                    chatService.getSession(sessionId)?.let { session ->
                        headerStatusSpan?.apply {
                            text = if (session.isOnline) "● 在线" else "○ 离线"
                            removeClassNames("text-success", "text-disabled")
                            addClassName(if (session.isOnline) "text-success" else "text-disabled")
                        }
                    }
                }
            }
        }
    }

    private fun createUserItem(session: ChatSession) = HorizontalLayout().apply {
        setWidthFull()
        addClassName("p-m")
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.START
        style.set("cursor", "pointer")
        style.set("border-bottom", "1px solid var(--lumo-contrast-5pct)")
        style.set("transition", "background-color 0.2s")

        if (session.sessionId == selectedSessionId) {
            style.set("background-color", "var(--lumo-primary-color-10pct)")
        }

        // 在线状态图标
        icon(if (session.isOnline) VaadinIcon.CIRCLE else VaadinIcon.CIRCLE_THIN) {
            style.set("width", "10px")
            style.set("height", "10px")
            addClassName(if (session.isOnline) "text-success" else "text-disabled")
        }

        // 用户名
        span(session.guestName) {
            style.set("font-weight", "600")
        }

        // 未读消息数
        if (session.unreadCount > 0) {
            span(session.unreadCount.toString()) {
                style.set("background", "var(--lumo-error-color)")
                style.set("color", "white")
                style.set("border-radius", "12px")
                style.set("padding", "2px 8px")
                style.set("font-size", "11px")
                style.set("font-weight", "600")
                style.set("margin-left", "auto")
            }
        }

        element.addEventListener("click") {
            selectSession(session.sessionId)
        }.addEventData("event.preventDefault()")
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

        chatArea.apply {
            // 头部
            horizontalLayout {
                alignItems = FlexComponent.Alignment.CENTER
                setWidthFull()
                addClassName("p-m")
                style.set("background-color", "white")
                style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")

                h3(session.guestName) {
                    style.set("margin", "0")
                }

                headerStatusSpan = span(if (session.isOnline) "● 在线" else "○ 离线") {
                    addClassName(if (session.isOnline) "text-success" else "text-disabled")
                }
            }

            // 消息区域
            messagesArea = verticalLayout {
                setWidthFull()
                addClassName("p-m")
                style.set("overflow-y", "auto")
            }

            // 输入区域
            var messageField: TextField? = null
            horizontalLayout {
                setWidthFull()
                addClassName("p-m")
                style.set("background-color", "white")
                style.set("box-shadow", "0 -2px 4px rgba(0,0,0,0.05)")

                messageField = textField {
                    placeholder = "输入消息..."
                    setWidthFull()
                    style.set("border-radius", "20px")
                }

                button("😊") {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    style.set("border-radius", "50%")
                    style.set("min-width", "40px")
                    style.set("font-size", "20px")
                    onLeftClick {
                        ui.ifPresent { ui ->
                            messageField?.let { EmojiPicker.show(ui, it, EmojiPicker.Position.RIGHT) }
                        }
                    }
                }

                button("发送") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    style.set("border-radius", "20px")
                    onLeftClick {
                        messageField?.let { sendMessage(sessionId, it, session) }
                    }
                }
            }

            messageField?.addKeyPressListener(Key.ENTER, {
                messageField?.let { sendMessage(sessionId, it, session) }
            })

            this@apply.expand(messagesArea)
        }

        refreshMessages(session)
    }

    private fun sendMessage(sessionId: String, messageField: TextField, session: ChatSession) {
        val content = messageField.value?.trim() ?: ""
        if (content.isNotEmpty()) {
            chatService.sendAdminMessage(sessionId, content)
            messageField.clear()
            refreshMessages(session)
            refreshUserList()
        }
    }

    private fun refreshMessages(session: ChatSession) {
        ui.ifPresent { ui ->
            ui.access {
                messagesArea.removeAll()
                session.messages.forEach { msg ->
                    val isAdmin = msg.from == "客服"

                    messagesArea.horizontalLayout {
                        setWidthFull()
                        justifyContentMode = if (isAdmin) FlexComponent.JustifyContentMode.END
                        else FlexComponent.JustifyContentMode.START

                        div {
                            addClassName("p-m")
                            style.set("border-radius", "12px")
                            style.set("max-width", "70%")
                            style.set("word-wrap", "break-word")

                            if (isAdmin) {
                                addClassNames("bg-primary", "text-primary-contrast")
                                style.set("box-shadow", "0 1px 3px rgba(0,0,0,0.2)")
                            } else {
                                style.set("background-color", "white")
                                style.set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)")
                            }

                            span(msg.from) {
                                addClassName("text-xs")
                                style.set("font-weight", "600")
                                style.set("display", "block")
                                style.set("margin-bottom", "4px")
                                if (isAdmin) style.set("opacity", "0.9")
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
            }
        }
    }
}
