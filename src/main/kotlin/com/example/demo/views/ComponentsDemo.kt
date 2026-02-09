package com.example.demo.views

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.router.Route

@Route("components")
class ComponentsDemo : KComposite() {
    private val root = ui {
        verticalLayout {
            setSizeFull()
            isPadding = true

            h1("Vaadin 组件演示")

            // 按钮示例
            h3("按钮")
            horizontalLayout {
                button("主要按钮") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                }
                button("次要按钮")
                button("危险按钮") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                }
            }

            // 输入框示例
            h3("输入框")
            textField("姓名") {
                placeholder = "请输入姓名"
                width = "300px"
            }

            emailField("邮箱") {
                placeholder = "example@email.com"
                width = "300px"
            }

            passwordField("密码") {
                width = "300px"
            }

            // 选择框示例
            h3("选择框")
            comboBox<String>("选择城市") {
                setItems("北京", "上海", "广州", "深圳")
                width = "300px"
            }

            // 复选框和单选框
            h3("复选框和单选框")
            checkBox("我同意条款和条件")

            radioButtonGroup<String> {
                label = "选择性别"
                setItems("男", "女", "其他")
            }

            // 日期选择器
            h3("日期选择器")
            datePicker("选择日期") {
                width = "300px"
            }

            // 文本区域
            h3("文本区域")
            textArea("备注") {
                placeholder = "输入多行文本..."
                width = "300px"
                height = "100px"
            }
        }
    }
}
