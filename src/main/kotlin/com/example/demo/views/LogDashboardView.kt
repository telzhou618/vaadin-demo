package com.example.demo.views

import com.example.demo.entity.LogLevel
import com.example.demo.service.LogQueryService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.Route
import java.time.format.DateTimeFormatter

/**
 * 日志仪表板视图
 * 提供日志统计、趋势分析和快速查询入口
 * 使用纯 CSS/HTML 实现图表，无需商业许可
 */
@Route("log-dashboard")
class LogDashboardView(
    private val logQueryService: LogQueryService
) : KComposite() {

    private lateinit var contentDiv: Div
    private lateinit var tabs: Tabs

    private val root = ui {
        verticalLayout {
            setSizeFull()
            setPadding(true)

            // 标题栏
            horizontalLayout {
                setWidthFull()
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

                h2("日志监控仪表板") {
                    style.set("margin", "0")
                }

                button("查询日志") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    icon = VaadinIcon.SEARCH.create()
                    onLeftClick {
                        ui.ifPresent { it.navigate(LogQueryView::class.java) }
                    }
                }
            }

            // 统计卡片
            horizontalLayout {
                setWidthFull()
                style.set("gap", "var(--lumo-space-m)")

                addStatCard(this, "总日志数", "12,458", VaadinIcon.FILE_TEXT, "#2196F3")
                addStatCard(this, "错误", "234", VaadinIcon.CLOSE_CIRCLE, "#f44336")
                addStatCard(this, "警告", "1,567", VaadinIcon.WARNING, "#ff9800")
                addStatCard(this, "平均响应", "45ms", VaadinIcon.CLOCK, "#4caf50")
            }

            // 标签页
            tabs = tabs {
                setWidthFull()

                tab("趋势分析")
                tab("级别分布")
                tab("来源统计")
                tab("实时监控")

                addSelectedChangeListener { event ->
                    updateContent(event.selectedTab)
                }
            }

            // 内容区域
            contentDiv = div {
                setSizeFull()
                style.set("overflow", "auto")
            }
        }
    }

    init {
        updateContent(tabs.selectedTab)
    }

    private fun addStatCard(layout: HorizontalLayout, title: String, value: String, icon: VaadinIcon, color: String) {
        layout.apply {
            div {
                style.apply {
                    set("background", "var(--lumo-contrast-5pct)")
                    set("padding", "var(--lumo-space-m)")
                    set("border-radius", "var(--lumo-border-radius-m)")
                    set("border-left", "4px solid $color")
                    set("flex", "1")
                }

                verticalLayout {
                    isPadding = false
                    isSpacing = false

                    horizontalLayout {
                        setWidthFull()
                        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

                        span(title) {
                            style.set("color", "var(--lumo-secondary-text-color)")
                            style.set("font-size", "var(--lumo-font-size-s)")
                        }

                        icon.create().apply {
                            style.set("margin-left", "auto")
                            style.set("color", color)
                        }
                    }

                    span(value) {
                        style.set("font-size", "var(--lumo-font-size-xxl)")
                        style.set("font-weight", "bold")
                        style.set("color", color)
                    }
                }
            }
        }
    }

    private fun updateContent(selectedTab: Tab) {
        contentDiv.removeAll()

        when (tabs.indexOf(selectedTab)) {
            0 -> showTrendChart()
            1 -> showLevelDistribution()
            2 -> showSourceStatistics()
            3 -> showRealTimeMonitor()
        }
    }

    private fun showTrendChart() {
        val chartComponent = createTrendChart()
        contentDiv.add(chartComponent)
    }

    private fun createTrendChart(): Div {
        return Div().apply {
            setSizeFull()

            val titleDiv = Div()
            titleDiv.text = "日志趋势分析（最近24小时）"
            titleDiv.style.apply {
                set("font-size", "var(--lumo-font-size-xl)")
                set("font-weight", "bold")
                set("margin-bottom", "var(--lumo-space-m)")
            }
            add(titleDiv)

            // 创建简单的柱状图
            val chartContainer = Div()
            chartContainer.style.apply {
                set("display", "flex")
                set("align-items", "flex-end")
                set("height", "400px")
                set("gap", "4px")
                set("padding", "var(--lumo-space-m)")
                set("background", "var(--lumo-contrast-5pct)")
                set("border-radius", "var(--lumo-border-radius-m)")
            }

            // 生成24小时的数据
            (0..23).forEach { hour ->
                val barContainer = Div()
                barContainer.style.apply {
                    set("flex", "1")
                    set("display", "flex")
                    set("flex-direction", "column")
                    set("align-items", "center")
                    set("gap", "4px")
                }

                // 堆叠的柱子
                val stackedBar = Div()
                stackedBar.style.apply {
                    set("width", "100%")
                    set("display", "flex")
                    set("flex-direction", "column-reverse")
                    set("gap", "1px")
                }

                val errorHeight = (50..150).random()
                val warnHeight = (100..300).random()
                val infoHeight = (200..500).random()
                val debugHeight = (300..600).random()
                val total = errorHeight + warnHeight + infoHeight + debugHeight
                val maxHeight = 1500

                // ERROR
                val errorBar = Div()
                errorBar.style.apply {
                    set("background", "#f44336")
                    set("height", "${(errorHeight.toDouble() / maxHeight * 300)}px")
                    set("border-radius", "2px 2px 0 0")
                }
                errorBar.element.setAttribute("title", "ERROR: $errorHeight")
                stackedBar.add(errorBar)

                // WARN
                val warnBar = Div()
                warnBar.style.apply {
                    set("background", "#ff9800")
                    set("height", "${(warnHeight.toDouble() / maxHeight * 300)}px")
                }
                warnBar.element.setAttribute("title", "WARN: $warnHeight")
                stackedBar.add(warnBar)

                // INFO
                val infoBar = Div()
                infoBar.style.apply {
                    set("background", "#4caf50")
                    set("height", "${(infoHeight.toDouble() / maxHeight * 300)}px")
                }
                infoBar.element.setAttribute("title", "INFO: $infoHeight")
                stackedBar.add(infoBar)

                // DEBUG
                val debugBar = Div()
                debugBar.style.apply {
                    set("background", "#2196F3")
                    set("height", "${(debugHeight.toDouble() / maxHeight * 300)}px")
                }
                debugBar.element.setAttribute("title", "DEBUG: $debugHeight")
                stackedBar.add(debugBar)

                barContainer.add(stackedBar)

                // 时间标签
                if (hour % 3 == 0) {
                    val label = Div()
                    label.text = "${hour}:00"
                    label.style.apply {
                        set("font-size", "0.75em")
                        set("color", "var(--lumo-secondary-text-color)")
                    }
                    barContainer.add(label)
                }

                chartContainer.add(barContainer)
            }

            add(chartContainer)

            // 图例
            val legend = Div()
            legend.style.apply {
                set("display", "flex")
                set("gap", "var(--lumo-space-m)")
                set("margin-top", "var(--lumo-space-m)")
                set("justify-content", "center")
            }

            listOf(
                "ERROR" to "#f44336",
                "WARN" to "#ff9800",
                "INFO" to "#4caf50",
                "DEBUG" to "#2196F3"
            ).forEach { (name, color) ->
                val item = Div()
                item.style.apply {
                    set("display", "flex")
                    set("align-items", "center")
                    set("gap", "4px")
                }

                val colorBox = Div()
                colorBox.style.apply {
                    set("width", "16px")
                    set("height", "16px")
                    set("background", color)
                    set("border-radius", "2px")
                }
                item.add(colorBox)

                val nameSpan = Div()
                nameSpan.text = name
                nameSpan.style.set("font-size", "0.9em")
                item.add(nameSpan)

                legend.add(item)
            }

            add(legend)
        }
    }

    private fun showLevelDistribution() {
        val chartComponent = createLevelDistributionChart()
        contentDiv.add(chartComponent)
    }

    private fun createLevelDistributionChart(): Div {
        return Div().apply {
            setSizeFull()

            val titleDiv = Div()
            titleDiv.text = "日志级别分布"
            titleDiv.style.apply {
                set("font-size", "var(--lumo-font-size-xl)")
                set("font-weight", "bold")
                set("margin-bottom", "var(--lumo-space-m)")
            }
            add(titleDiv)

            val stats = mapOf(
                LogLevel.ERROR to 234,
                LogLevel.WARN to 1567,
                LogLevel.INFO to 8234,
                LogLevel.DEBUG to 2423
            )

            val total = stats.values.sum()

            // 饼图容器
            val pieContainer = Div()
            pieContainer.style.apply {
                set("display", "flex")
                set("justify-content", "center")
                set("align-items", "center")
                set("gap", "var(--lumo-space-xl)")
                set("padding", "var(--lumo-space-l)")
            }

            // 简单的环形图
            val donutChart = Div()
            donutChart.style.apply {
                set("width", "300px")
                set("height", "300px")
                set("border-radius", "50%")
                set("position", "relative")
                set("background", "conic-gradient(" +
                        "#f44336 0% ${234.0 / total * 100}%, " +
                        "#ff9800 ${234.0 / total * 100}% ${(234.0 + 1567) / total * 100}%, " +
                        "#4caf50 ${(234.0 + 1567) / total * 100}% ${(234.0 + 1567 + 8234) / total * 100}%, " +
                        "#2196F3 ${(234.0 + 1567 + 8234) / total * 100}% 100%)")
            }

            // 中心白色圆
            val centerCircle = Div()
            centerCircle.style.apply {
                set("position", "absolute")
                set("top", "50%")
                set("left", "50%")
                set("transform", "translate(-50%, -50%)")
                set("width", "180px")
                set("height", "180px")
                set("border-radius", "50%")
                set("background", "white")
                set("display", "flex")
                set("flex-direction", "column")
                set("justify-content", "center")
                set("align-items", "center")
            }

            val totalLabel = Div()
            totalLabel.text = "总计"
            totalLabel.style.apply {
                set("font-size", "0.9em")
                set("color", "var(--lumo-secondary-text-color)")
            }
            centerCircle.add(totalLabel)

            val totalValue = Div()
            totalValue.text = total.toString()
            totalValue.style.apply {
                set("font-size", "2em")
                set("font-weight", "bold")
            }
            centerCircle.add(totalValue)

            donutChart.add(centerCircle)
            pieContainer.add(donutChart)

            // 统计列表
            val statsList = Div()
            statsList.style.apply {
                set("display", "flex")
                set("flex-direction", "column")
                set("gap", "var(--lumo-space-m)")
            }

            stats.forEach { (level, count) ->
                val percentage = (count.toDouble() / total * 100).toInt()

                val item = Div()
                item.style.apply {
                    set("display", "flex")
                    set("align-items", "center")
                    set("gap", "var(--lumo-space-s)")
                }

                val colorBox = Div()
                colorBox.style.apply {
                    set("width", "20px")
                    set("height", "20px")
                    set("background", level.color)
                    set("border-radius", "4px")
                }
                item.add(colorBox)

                val info = Div()
                info.style.apply {
                    set("display", "flex")
                    set("flex-direction", "column")
                }

                val nameDiv = Div()
                nameDiv.text = level.displayName
                nameDiv.style.set("font-weight", "bold")
                info.add(nameDiv)

                val valueDiv = Div()
                valueDiv.text = "$count ($percentage%)"
                valueDiv.style.apply {
                    set("font-size", "0.9em")
                    set("color", "var(--lumo-secondary-text-color)")
                }
                info.add(valueDiv)

                item.add(info)
                statsList.add(item)
            }

            pieContainer.add(statsList)
            add(pieContainer)
        }
    }

    private fun showSourceStatistics() {
        val chartComponent = createSourceStatisticsChart()
        contentDiv.add(chartComponent)
    }

    private fun createSourceStatisticsChart(): Div {
        return Div().apply {
            setSizeFull()

            val titleDiv = Div()
            titleDiv.text = "日志来源统计"
            titleDiv.style.apply {
                set("font-size", "var(--lumo-font-size-xl)")
                set("font-weight", "bold")
                set("margin-bottom", "var(--lumo-space-m)")
            }
            add(titleDiv)

            val sources = mapOf(
                "app-server-01" to mapOf("ERROR" to 50, "WARN" to 300, "INFO" to 2000),
                "app-server-02" to mapOf("ERROR" to 80, "WARN" to 450, "INFO" to 2500),
                "worker-01" to mapOf("ERROR" to 45, "WARN" to 280, "INFO" to 1800),
                "worker-02" to mapOf("ERROR" to 60, "WARN" to 350, "INFO" to 2200)
            )

            val maxTotal = sources.values.maxOf { it.values.sum() }

            sources.forEach { (source, data) ->
                val total = data.values.sum()

                val sourceContainer = Div()
                sourceContainer.style.apply {
                    set("margin-bottom", "var(--lumo-space-m)")
                    set("padding", "var(--lumo-space-m)")
                    set("background", "var(--lumo-contrast-5pct)")
                    set("border-radius", "var(--lumo-border-radius-m)")
                }

                val header = Div()
                header.style.apply {
                    set("display", "flex")
                    set("justify-content", "space-between")
                    set("margin-bottom", "var(--lumo-space-s)")
                }

                val sourceName = Div()
                sourceName.text = source
                sourceName.style.set("font-weight", "bold")
                header.add(sourceName)

                val totalValue = Div()
                totalValue.text = "$total 条"
                totalValue.style.set("color", "var(--lumo-secondary-text-color)")
                header.add(totalValue)

                sourceContainer.add(header)

                // 堆叠条形图
                val barContainer = Div()
                barContainer.style.apply {
                    set("display", "flex")
                    set("height", "40px")
                    set("border-radius", "4px")
                    set("overflow", "hidden")
                }

                val errorWidth = (data["ERROR"] ?: 0).toDouble() / maxTotal * 100
                val warnWidth = (data["WARN"] ?: 0).toDouble() / maxTotal * 100
                val infoWidth = (data["INFO"] ?: 0).toDouble() / maxTotal * 100

                val errorBar = Div()
                errorBar.style.apply {
                    set("width", "$errorWidth%")
                    set("background", "#f44336")
                    set("display", "flex")
                    set("align-items", "center")
                    set("justify-content", "center")
                    set("color", "white")
                    set("font-size", "0.8em")
                }
                if (errorWidth > 5) errorBar.text = data["ERROR"].toString()
                barContainer.add(errorBar)

                val warnBar = Div()
                warnBar.style.apply {
                    set("width", "$warnWidth%")
                    set("background", "#ff9800")
                    set("display", "flex")
                    set("align-items", "center")
                    set("justify-content", "center")
                    set("color", "white")
                    set("font-size", "0.8em")
                }
                if (warnWidth > 5) warnBar.text = data["WARN"].toString()
                barContainer.add(warnBar)

                val infoBar = Div()
                infoBar.style.apply {
                    set("width", "$infoWidth%")
                    set("background", "#4caf50")
                    set("display", "flex")
                    set("align-items", "center")
                    set("justify-content", "center")
                    set("color", "white")
                    set("font-size", "0.8em")
                }
                if (infoWidth > 5) infoBar.text = data["INFO"].toString()
                barContainer.add(infoBar)

                sourceContainer.add(barContainer)
                add(sourceContainer)
            }
        }
    }

    private fun showRealTimeMonitor() {
        val monitorComponent = createRealTimeMonitor()
        contentDiv.add(monitorComponent)
    }

    private fun createRealTimeMonitor(): Div {
        return Div().apply {
            setSizeFull()

            val titleDiv = Div()
            titleDiv.text = "实时日志监控"
            titleDiv.style.apply {
                set("font-size", "var(--lumo-font-size-xl)")
                set("font-weight", "bold")
                set("margin-bottom", "var(--lumo-space-m)")
            }
            add(titleDiv)

            val logContainer = Div()
            logContainer.style.apply {
                set("background", "var(--lumo-contrast-5pct)")
                set("padding", "var(--lumo-space-m)")
                set("border-radius", "var(--lumo-border-radius-m)")
                set("height", "400px")
                set("overflow-y", "auto")
                set("font-family", "monospace")
                set("font-size", "0.9em")
            }

            // 模拟实时日志
            val logs = logQueryService.queryLogs(limit = 20)
            logs.forEach { log ->
                val logEntry = Div()
                logEntry.style.apply {
                    set("margin-bottom", "8px")
                    set("padding", "8px")
                    set("background", "white")
                    set("border-radius", "4px")
                    set("border-left", "3px solid ${log.level.color}")
                }

                val timestamp = log.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                logEntry.text = "[$timestamp] [${log.level.displayName}] ${log.logger} - ${log.message}"
                logContainer.add(logEntry)
            }

            add(logContainer)

            // 控制按钮
            val buttonLayout = Div()
            buttonLayout.style.apply {
                set("margin-top", "var(--lumo-space-m)")
                set("display", "flex")
                set("gap", "var(--lumo-space-s)")
            }

            val pauseBtn = com.vaadin.flow.component.button.Button("暂停", VaadinIcon.PAUSE.create())
            val clearBtn = com.vaadin.flow.component.button.Button("清空", VaadinIcon.TRASH.create())
            val scrollBtn = com.vaadin.flow.component.button.Button("自动滚动", VaadinIcon.ARROW_DOWN.create())

            buttonLayout.add(pauseBtn, clearBtn, scrollBtn)
            add(buttonLayout)
        }
    }
}
