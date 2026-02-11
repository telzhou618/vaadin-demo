package com.example.demo.views

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("wechat")
class WeChatView : KComposite() {
    private val root = ui {
        verticalLayout {
            setSizeFull()
            isPadding = false
            isMargin = false
            style.set("background-color", "#ededed")
            style.set("overflow", "hidden")

            // 顶部导航栏
            createTopBar()

            // 主内容区域
            horizontalLayout {
                setSizeFull()
                isPadding = false
                isMargin = false
                style.set("overflow", "hidden")

                // 左侧会话列表
                createChatList()

                // 右侧聊天窗口
                createChatWindow()
            }
        }
    }

    private fun VerticalLayout.createTopBar() {
        horizontalLayout {
            width = "100%"
            height = "60px"
            style.set("background-color", "#2e2e2e")
            style.set("color", "white")
            alignItems = FlexComponent.Alignment.CENTER
            isPadding = true

            h3("微信") {
                style.set("margin", "0")
                style.set("color", "white")
            }

            div {
                style.set("margin-left", "auto")
                style.set("display", "flex")
                style.set("gap", "20px")

                button {
                    icon = VaadinIcon.PLUS.create()
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    style.set("color", "white")
                }

                button {
                    icon = VaadinIcon.SEARCH.create()
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    style.set("color", "white")
                }

                button {
                    icon = VaadinIcon.COG.create()
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    style.set("color", "white")
                }
            }
        }
    }

    private fun HorizontalLayout.createChatList() {
        verticalLayout {
            width = "300px"
            height = "100%"
            isPadding = false
            isMargin = false
            style.set("background-color", "white")
            style.set("border-right", "1px solid #e0e0e0")
            style.set("overflow-y", "auto")
            style.set("overflow-x", "hidden")
            style.set("flex-shrink", "0")

            // 搜索框
            horizontalLayout {
                width = "100%"
                isPadding = true
                isMargin = false
                style.set("background-color", "white")
                style.set("border-bottom", "1px solid #f0f0f0")
                
                textField {
                    width = "100%"
                    placeholder = "搜索"
                    prefixComponent = VaadinIcon.SEARCH.create()
                    style.set("--vaadin-field-default-width", "100%")
                }
            }

            // 会话列表
            val chatSessions = listOf(
                ChatSession("张三", "你好，在吗？", "15:30", true),
                ChatSession("李四", "明天见", "14:20", false),
                ChatSession("王五", "收到，谢谢", "昨天", false),
                ChatSession("赵六", "好的", "星期一", false),
                ChatSession("产品经理", "需求文档已发送", "星期日", false),
                ChatSession("技术群", "今天下午开会", "星期六", false),
                ChatSession("小美", "[图片]", "星期五", false)
            )

            chatSessions.forEach { session ->
                createChatSessionItem(session)
            }
        }
    }

    private fun VerticalLayout.createChatSessionItem(session: ChatSession) {
        horizontalLayout {
            width = "100%"
            isPadding = true
            isMargin = false
            style.set("cursor", "pointer")
            style.set("border-bottom", "1px solid #f0f0f0")
            style.set("min-height", "70px")
            alignItems = FlexComponent.Alignment.CENTER

            element.addEventListener("mouseenter") {
                style.set("background-color", "#f5f5f5")
            }
            element.addEventListener("mouseleave") {
                style.set("background-color", "white")
            }

            // 头像
            div {
                width = "45px"
                height = "45px"
                style.set("background-color", "#07c160")
                style.set("border-radius", "4px")
                style.set("display", "flex")
                style.set("align-items", "center")
                style.set("justify-content", "center")
                style.set("color", "white")
                style.set("font-weight", "bold")
                style.set("flex-shrink", "0")

                text(session.name.first().toString())
            }

            // 消息内容
            verticalLayout {
                isPadding = false
                isMargin = false
                style.set("flex", "1")
                style.set("margin-left", "10px")
                style.set("min-width", "0")
                style.set("overflow", "hidden")

                horizontalLayout {
                    width = "100%"
                    isPadding = false
                    isMargin = false
                    justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
                    alignItems = FlexComponent.Alignment.CENTER

                    span(session.name) {
                        style.set("font-weight", if (session.unread) "bold" else "normal")
                        style.set("font-size", "14px")
                    }

                    span(session.time) {
                        style.set("font-size", "12px")
                        style.set("color", "#999")
                        style.set("flex-shrink", "0")
                        style.set("margin-left", "10px")
                    }
                }

                span(session.lastMessage) {
                    style.set("font-size", "13px")
                    style.set("color", "#999")
                    style.set("overflow", "hidden")
                    style.set("text-overflow", "ellipsis")
                    style.set("white-space", "nowrap")
                    style.set("margin-top", "4px")
                }
            }

            // 未读标记
            if (session.unread) {
                div {
                    width = "8px"
                    height = "8px"
                    style.set("background-color", "#fa5151")
                    style.set("border-radius", "50%")
                    style.set("margin-left", "8px")
                    style.set("flex-shrink", "0")
                }
            }
        }
    }


    private fun HorizontalLayout.createChatWindow() {
        verticalLayout {
            setSizeFull()
            isPadding = false
            isMargin = false
            style.set("background-color", "#f5f5f5")
            style.set("overflow", "hidden")

            // 聊天窗口顶部
            horizontalLayout {
                width = "100%"
                height = "60px"
                style.set("background-color", "white")
                style.set("border-bottom", "1px solid #e0e0e0")
                style.set("flex-shrink", "0")
                alignItems = FlexComponent.Alignment.CENTER
                isPadding = true

                span("张三") {
                    style.set("font-size", "16px")
                    style.set("font-weight", "bold")
                }

                div {
                    style.set("margin-left", "auto")
                    style.set("display", "flex")
                    style.set("gap", "15px")

                    button {
                        icon = VaadinIcon.PHONE.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    }

                    button {
                        icon = VaadinIcon.CAMERA.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    }

                    button {
                        icon = VaadinIcon.ELLIPSIS_DOTS_H.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    }
                }
            }

            // 消息区域
            val messageArea = div {
                width = "100%"
                style.set("flex", "1")
                style.set("overflow-y", "auto")
                style.set("overflow-x", "hidden")
                style.set("padding", "20px")

                // 示例消息
                createMessageBubble("你好，在吗？", false, "15:28")
                createMessageBubble("在的，有什么事吗？", true, "15:29")
                createMessageBubble("想问一下关于项目的事情", false, "15:30")
                createMessageBubble("好的，你说", true, "15:30")
            }

            // 输入区域
            verticalLayout {
                width = "100%"
                height = "200px"
                isPadding = false
                isMargin = false
                style.set("background-color", "white")
                style.set("border-top", "1px solid #e0e0e0")
                style.set("flex-shrink", "0")

                // 工具栏
                horizontalLayout {
                    width = "100%"
                    height = "40px"
                    isPadding = true
                    isMargin = false
                    style.set("border-bottom", "1px solid #f0f0f0")
                    style.set("gap", "10px")
                    style.set("flex-shrink", "0")

                    button {
                        icon = VaadinIcon.SMILEY_O.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
                    }

                    button {
                        icon = VaadinIcon.FOLDER_O.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
                    }

                    button {
                        icon = VaadinIcon.CAMERA.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
                    }

                    button {
                        icon = VaadinIcon.PICTURE.create()
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
                    }
                }

                // 输入框
                val inputArea = textArea {
                    width = "100%"
                    style.set("flex", "1")
                    style.set("border", "none")
                    style.set("resize", "none")
                    style.set("outline", "none")
                    placeholder = "输入消息..."
                }

                // 发送按钮
                horizontalLayout {
                    width = "100%"
                    height = "50px"
                    isPadding = true
                    isMargin = false
                    style.set("flex-shrink", "0")
                    justifyContentMode = FlexComponent.JustifyContentMode.END

                    button("发送") {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        style.set("border-radius", "4px")
                        onLeftClick {
                            val message = inputArea.value
                            if (message.isNotBlank()) {
                                messageArea.createMessageBubble(message, true, "刚刚")
                                inputArea.clear()
                                // 滚动到底部
                                messageArea.element.executeJs("this.scrollTop = this.scrollHeight")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Div.createMessageBubble(text: String, isSelf: Boolean, time: String) {
        horizontalLayout {
            width = "100%"
            isPadding = false
            isMargin = false
            style.set("margin-bottom", "15px")
            justifyContentMode =
                if (isSelf) FlexComponent.JustifyContentMode.END else FlexComponent.JustifyContentMode.START

            if (!isSelf) {
                // 对方头像
                div {
                    width = "40px"
                    height = "40px"
                    style.set("background-color", "#07c160")
                    style.set("border-radius", "4px")
                    style.set("display", "flex")
                    style.set("align-items", "center")
                    style.set("justify-content", "center")
                    style.set("color", "white")
                    style.set("font-weight", "bold")
                    style.set("flex-shrink", "0")
                    style.set("margin-right", "10px")

                    text("张")
                }
            }

            verticalLayout {
                isPadding = false
                isMargin = false
                style.set("max-width", "60%")
                style.set("min-width", "0")
                alignItems = if (isSelf) FlexComponent.Alignment.END else FlexComponent.Alignment.START

                div {
                    style.set("background-color", if (isSelf) "#95ec69" else "white")
                    style.set("padding", "10px 15px")
                    style.set("border-radius", "4px")
                    style.set("word-wrap", "break-word")
                    style.set("word-break", "break-word")
                    style.set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)")
                    style.set("line-height", "1.5")

                    text(text)
                }

                span(time) {
                    style.set("font-size", "11px")
                    style.set("color", "#999")
                    style.set("margin-top", "5px")
                }
            }

            if (isSelf) {
                // 自己的头像
                div {
                    width = "40px"
                    height = "40px"
                    style.set("background-color", "#1989fa")
                    style.set("border-radius", "4px")
                    style.set("display", "flex")
                    style.set("align-items", "center")
                    style.set("justify-content", "center")
                    style.set("color", "white")
                    style.set("font-weight", "bold")
                    style.set("flex-shrink", "0")
                    style.set("margin-left", "10px")

                    text("我")
                }
            }
        }
    }

    data class ChatSession(
        val name: String,
        val lastMessage: String,
        val time: String,
        val unread: Boolean
    )
}
