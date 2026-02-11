package com.example.demo.views

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.placeholder
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.tabs.TabsVariant
import com.vaadin.flow.router.Route

@Route("components")
class ComponentsDemo : KComposite() {
    private val root = ui {
        verticalLayout {
            setSizeFull()
            isPadding = true

            h1("Vaadin 组件演示")

            // 按钮示例
            h2("按钮 (Buttons)")
            horizontalLayout {
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                style.set("gap", "var(--lumo-space-s)")

                button("主要按钮") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    onLeftClick {
                        Notification.show("点击了主要按钮")
                    }
                }
                button("次要按钮") {
                    onLeftClick {
                        Notification.show("点击了次要按钮")
                    }
                }
                button("成功按钮") {
                    addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                }
                button("危险按钮") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                }
                button("对比按钮") {
                    addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                }
            }

            horizontalLayout {
                style.set("gap", "var(--lumo-space-s)")

                button("小按钮") {
                    addThemeVariants(ButtonVariant.LUMO_SMALL)
                }
                button("大按钮") {
                    addThemeVariants(ButtonVariant.LUMO_LARGE)
                }
                button("带图标") {
                    icon = VaadinIcon.STAR.create()
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                }
                button {
                    icon = VaadinIcon.HEART.create()
                    addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR)
                }
            }

            hr()

            // 输入框示例
            h2("输入框 (Text Fields)")
            formLayout {
                textField("姓名") {
                    placeholder = "请输入姓名"
                    helperText = "输入您的真实姓名"
                }

                emailField("邮箱") {
                    placeholder = "example@email.com"
                    isRequired = true
                    errorMessage = "请输入有效的邮箱地址"
                }

                passwordField("密码") {
                    helperText = "至少8个字符"
                }

                numberField("年龄") {
                    min = 0.0
                    max = 150.0
                    step = 1.0
                }

                textField("带前缀") {
                    prefixComponent = VaadinIcon.USER.create()
                }

                textField("带后缀") {
                    suffixComponent = VaadinIcon.SEARCH.create()
                    isClearButtonVisible = true
                }
            }

            hr()

            // 选择框示例
            h2("选择框 (Select Components)")
            formLayout {
                comboBox<String>("城市") {
                    setItems("北京", "上海", "广州", "深圳", "杭州", "成都")
                    placeholder = "选择城市"
                }

                select<String> {
                    label = "国家"
                    setItems("中国", "美国", "日本", "英国", "法国")
                }
            }

            hr()

            // 复选框和单选框
            h2("复选框和单选框 (Checkboxes & Radio Buttons)")
            verticalLayout {
                isPadding = false

                checkBox("我同意条款和条件") {
                    onLeftClick {
                        Notification.show("复选框状态: ${value}")
                    }
                }

                checkBox("启用通知")
                checkBox("记住我")

                radioButtonGroup<String> {
                    label = "选择性别"
                    setItems("男", "女", "其他")
                }

                radioButtonGroup<String> {
                    label = "订阅计划"
                    setItems("免费", "基础版", "专业版", "企业版")
                    addThemeVariants(com.vaadin.flow.component.radiobutton.RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD)
                }

                checkBoxGroup<String> {
                    label = "选择权限"
                    setItems("读取", "写入", "删除", "管理")
                }
            }

            hr()

            // 日期和时间选择器
            h2("日期和时间 (Date & Time Pickers)")
            formLayout {
                datePicker("出生日期") {
                    placeholder = "选择日期"
                }

                timePicker("预约时间") {
                    placeholder = "选择时间"
                }
            }
            
            val dateTimePickerComponent = com.vaadin.flow.component.datetimepicker.DateTimePicker("会议时间")
            add(dateTimePickerComponent)

            hr()

            // 文本区域和富文本
            h2("文本区域 (Text Area)")
            textArea("备注") {
                placeholder = "输入多行文本..."
                width = "100%"
                height = "120px"
                maxLength = 500
                helperText = "最多500个字符"
            }

            hr()

            // 通知示例
            h2("通知 (Notifications)")
            horizontalLayout {
                style.set("gap", "var(--lumo-space-s)")

                button("默认通知") {
                    onLeftClick {
                        Notification.show("这是一条默认通知")
                    }
                }

                button("成功通知") {
                    addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                    onLeftClick {
                        val notification = Notification.show("操作成功！")
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS)
                    }
                }

                button("错误通知") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                    onLeftClick {
                        val notification = Notification.show("发生错误！")
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR)
                    }
                }

                button("警告通知") {
                    addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                    onLeftClick {
                        val notification = Notification.show("警告信息")
                        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST)
                    }
                }
            }

            hr()

            // 进度条和加载指示器
            h2("进度条 (Progress Bar)")
            verticalLayout {
                isPadding = false

                span("确定进度:")
                val progressBar1 = ProgressBar()
                progressBar1.value = 0.6
                progressBar1.width = "300px"
                add(progressBar1)

                span("不确定进度:")
                val progressBar2 = ProgressBar()
                progressBar2.isIndeterminate = true
                progressBar2.width = "300px"
                add(progressBar2)
            }

            hr()

            // 标签页
            h2("标签页 (Tabs)")
            tabs {
                tab("首页") {
                    add(VaadinIcon.HOME.create())
                }
                tab("用户")
                tab("设置")
                tab("关于")

                addSelectedChangeListener {
                    Notification.show("切换到: ${it.selectedTab.label}")
                }
            }

            tabs {
                addThemeVariants(TabsVariant.LUMO_CENTERED)
                tab("居中标签1")
                tab("居中标签2")
                tab("居中标签3")
            }

            hr()

            // 徽章和标签
            h2("徽章和标签 (Badges & Tags)")
            horizontalLayout {
                style.set("gap", "var(--lumo-space-s)")

                span("新") {
                    element.themeList.add("badge")
                    element.themeList.add("success")
                }

                span("热门") {
                    element.themeList.add("badge")
                    element.themeList.add("error")
                }

                span("推荐") {
                    element.themeList.add("badge")
                    element.themeList.add("contrast")
                }

                span("99+") {
                    element.themeList.add("badge")
                    element.themeList.add("pill")
                }
            }

            hr()

            // 对话框
            h2("对话框 (Dialog)")
            horizontalLayout {
                style.set("gap", "var(--lumo-space-s)")

                button("打开对话框") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    onLeftClick {
                        val dlg = com.vaadin.flow.component.dialog.Dialog()
                        dlg.headerTitle = "对话框标题"

                        val content = com.vaadin.flow.component.orderedlayout.VerticalLayout()
                        content.add(com.vaadin.flow.component.html.Span("这是对话框的内容"))
                        content.add(com.vaadin.flow.component.textfield.TextField("输入内容"))
                        dlg.add(content)

                        val cancelBtn = com.vaadin.flow.component.button.Button("取消") { dlg.close() }
                        val confirmBtn = com.vaadin.flow.component.button.Button("确定") {
                            Notification.show("已确认")
                            dlg.close()
                        }
                        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        dlg.footer.add(cancelBtn, confirmBtn)

                        dlg.open()
                    }
                }

                button("简单确认") {
                    onLeftClick {
                        val dlg = com.vaadin.flow.component.dialog.Dialog()
                        dlg.headerTitle = "确认操作"
                        
                        val content = com.vaadin.flow.component.orderedlayout.VerticalLayout()
                        content.add(com.vaadin.flow.component.html.Span("您确定要执行此操作吗？"))
                        dlg.add(content)
                        
                        val cancelBtn = com.vaadin.flow.component.button.Button("取消") { dlg.close() }
                        val confirmBtn = com.vaadin.flow.component.button.Button("确定") {
                            Notification.show("已确认")
                            dlg.close()
                        }
                        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        dlg.footer.add(cancelBtn, confirmBtn)

                        dlg.open()
                    }
                }
            }

            hr()

            // 详情展开
            h2("详情展开 (Details)")
            details("点击展开更多信息") {
                content {
                    verticalLayout {
                        span("这里是展开后的详细内容")
                        span("可以包含任何组件")
                        button("操作按钮")
                    }
                }
            }

            details("展开查看代码示例") {
                content {
                    pre {
                        text = """
                            fun main() {
                                println("Hello, Vaadin!")
                            }
                        """.trimIndent()
                        style.apply {
                            set("background", "var(--lumo-contrast-5pct)")
                            set("padding", "var(--lumo-space-m)")
                            set("border-radius", "var(--lumo-border-radius-m)")
                        }
                    }
                }
            }

            hr()

            // 分割器
            h2("分割器 (Split Layout)")
            val split = com.vaadin.flow.component.splitlayout.SplitLayout()
            split.setHeight("300px")
            split.setWidthFull()

            val leftPanel = com.vaadin.flow.component.html.Div()
            leftPanel.style.apply {
                set("padding", "var(--lumo-space-m)")
                set("background", "var(--lumo-contrast-5pct)")
            }
            leftPanel.text = "左侧面板"

            val rightPanel = com.vaadin.flow.component.html.Div()
            rightPanel.style.apply {
                set("padding", "var(--lumo-space-m)")
                set("background", "var(--lumo-contrast-10pct)")
            }
            rightPanel.text = "右侧面板"

            split.addToPrimary(leftPanel)
            split.addToSecondary(rightPanel)
            add(split)

            hr()

            // 图标
            h2("图标 (Icons)")
            horizontalLayout {
                style.set("gap", "var(--lumo-space-m)")

                VaadinIcon.HOME.create().apply {
                    style.set("color", "var(--lumo-primary-color)")
                }
                VaadinIcon.USER.create().apply {
                    style.set("color", "var(--lumo-success-color)")
                }
                VaadinIcon.HEART.create().apply {
                    style.set("color", "var(--lumo-error-color)")
                }
                VaadinIcon.STAR.create().apply {
                    style.set("color", "#ffc107")
                }
                VaadinIcon.BELL.create()
                VaadinIcon.ENVELOPE.create()
                VaadinIcon.SEARCH.create()
                VaadinIcon.COG.create()
            }

            hr()

            // 上传组件
            h2("文件上传 (Upload)")
            upload {
                maxFiles = 5
                maxFileSize = 10 * 1024 * 1024 // 10MB
                setAcceptedFileTypes("image/*", ".pdf", ".doc", ".docx")

                addSucceededListener {
                    Notification.show("文件上传成功: ${it.fileName}")
                }

                addFailedListener {
                    Notification.show("文件上传失败: ${it.fileName}")
                }
            }

            hr()

            // 头像
            h2("头像 (Avatar)")
            horizontalLayout {
                style.set("gap", "var(--lumo-space-m)")

                avatar("张三")
                avatar("李四") {
                    colorIndex = 1
                }
                avatar("王五") {
                    colorIndex = 2
                }
                avatar {
                    name = "管理员"
                    image = "https://via.placeholder.com/150"
                }
            }

            hr()

            // 菜单栏
            h2("菜单栏 (Menu Bar)")
            menuBar {
                val fileItem = addItem("文件")
                val fileMenu = fileItem.subMenu
                fileMenu.addItem("新建")
                fileMenu.addItem("打开")
                fileMenu.addItem("保存")
                fileMenu.addItem("退出")

                val editItem = addItem("编辑")
                val editMenu = editItem.subMenu
                editMenu.addItem("撤销")
                editMenu.addItem("重做")
                editMenu.addItem("剪切")
                editMenu.addItem("复制")
                editMenu.addItem("粘贴")

                val helpItem = addItem("帮助")
                val helpMenu = helpItem.subMenu
                helpMenu.addItem("文档")
                helpMenu.addItem("关于")
            }

            hr()

            // 上下文菜单
            h2("上下文菜单 (Context Menu)")
            div {
                text = "右键点击这里查看上下文菜单"
                style.apply {
                    set("padding", "var(--lumo-space-l)")
                    set("background", "var(--lumo-contrast-5pct)")
                    set("border-radius", "var(--lumo-border-radius-m)")
                    set("cursor", "pointer")
                }

                contextMenu {
                    addItem("复制") {
                        Notification.show("复制")
                    }
                    addItem("粘贴") {
                        Notification.show("粘贴")
                    }
                    addItem("删除") {
                        Notification.show("删除")
                    }
                }
            }

            hr()

            span("更多组件请查看 Vaadin 官方文档")
        }
    }
}
