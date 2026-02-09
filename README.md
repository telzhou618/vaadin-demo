# Vaadin 24 Demo 项目

这是一个用于学习 Vaadin 24 的演示项目，使用 Kotlin DSL 和 Gradle 构建。

## 技术栈

- Vaadin 24.3.5
- Kotlin 1.9.22
- Karibu-DSL 2.1.2 (Vaadin Kotlin DSL)
- Gradle (Kotlin DSL)
- JDK 17+

## 项目结构

```
vaadin-demo/
├── src/main/kotlin/com/example/demo/
│   ├── Application.kt          # 应用入口
│   └── views/
│       └── MainView.kt         # 主视图
├── src/main/resources/
│   └── application.properties  # 配置文件
├── build.gradle.kts            # Gradle 构建配置
└── settings.gradle.kts         # Gradle 设置
```

## 运行项目

1. 确保已安装 JDK 17 或更高版本
2. 在项目根目录运行：

```bash
./gradlew run
```

或在 Windows 上：

```cmd
gradlew.bat run
```

3. 打开浏览器访问：http://localhost:8080

## 开发模式

项目默认运行在开发模式，支持热重载。修改代码后，刷新浏览器即可看到更改。

## 添加新的 Demo

在 `src/main/kotlin/com/example/demo/views/` 目录下创建新的视图类，使用 `@Route` 注解指定路由路径。

示例：

```kotlin
@Route("demo")
class DemoView : KComposite() {
    private val root = ui {
        verticalLayout {
            h2("新的 Demo")
            // 添加你的组件
        }
    }
}
```

## 有用的资源

- [Vaadin 官方文档](https://vaadin.com/docs/latest)
- [Karibu-DSL 文档](https://github.com/mvysny/karibu-dsl)
- [Vaadin 组件示例](https://vaadin.com/components)
