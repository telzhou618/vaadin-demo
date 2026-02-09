package com.example.demo.views

import com.example.demo.service.BroadcastService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import kotlin.concurrent.thread

@Route("push")
class PushDemo(@Autowired private val broadcastService: BroadcastService) : KComposite() {

    private val root = ui {
        verticalLayout {
            setSizeFull()
            isPadding = true

            h1("Vaadin Push 演示")

            span("这个页面演示服务器向所有客户端推送 Toast 消息")
            span("打开多个浏览器标签页，点击按钮后所有页面都会收到通知")

            hr()

            val messageField = textField("消息内容") {
                width = "400px"
                value = "你好，这是一条广播消息！"
            }

            button("向所有客户端发送消息") {
                addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY)
                onLeftClick {
                    val message = messageField.value
                    if (message.isNotBlank()) {
                        broadcastService.broadcast(message)
                    }
                }
            }

            hr()

            h3("自动广播演示")
            span("点击下面的按钮，服务器会每隔 3 秒向所有客户端发送一条消息")

            var counter = 0
            var isRunning = false

            horizontalLayout {
                button("开始自动广播") {
                    addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS)
                    onLeftClick {
                        if (!isRunning) {
                            isRunning = true
                            thread {
                                while (isRunning && counter < 10) {
                                    Thread.sleep(3000)
                                    counter++
                                    broadcastService.broadcast("自动广播消息 #$counter")
                                }
                                isRunning = false
                                counter = 0
                            }
                        }
                    }
                }

                button("停止广播") {
                    addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR)
                    onLeftClick {
                        isRunning = false
                        counter = 0
                    }
                }
            }

            hr()

            routerLink(text = "← 返回首页", viewType = MainView::class)
        }
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        ui.ifPresent { ui ->
            broadcastService.register(ui)
        }
    }

    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        ui.ifPresent { ui ->
            broadcastService.unregister(ui)
        }
    }
}
