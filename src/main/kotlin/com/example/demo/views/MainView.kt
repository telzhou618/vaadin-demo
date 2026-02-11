package com.example.demo.views

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.router.Route

@Route("")
class MainView : KComposite() {
    private val root = ui {
        verticalLayout {
            setSizeFull()
            isPadding = true

            h1("欢迎使用 Vaadin 24 Demo")

            span("这是一个用于学习 Vaadin 的演示项目")

            button("点击我") {
                onLeftClick {
                    Notification.show("你好，Vaadin!")
                }
            }

            horizontalLayout {
                textField("输入文本") {
                    placeholder = "在这里输入..."
                }

                button("提交") {
                    onLeftClick {
                        Notification.show("表单已提交")
                    }
                }
            }

            hr()

            h3("示例页面")
            routerLink(text = "组件演示", viewType = ComponentsDemo::class)
            routerLink(text = "Push 演示", viewType = PushDemo::class)
            
            hr()
            
            h3("客服系统 Demo")
            routerLink(text = "游客咨询页面", viewType = GuestChatView::class)
            routerLink(text = "客服管理端", viewType = AdminChatView::class)
            
            hr()
            
            h3("微信界面 Demo")
            routerLink(text = "微信网页版", viewType = WeChatView::class)
            
            hr()
            
            h3("日志查询系统")
            routerLink(text = "日志查询界面", viewType = LogQueryView::class)
        }
    }
}
