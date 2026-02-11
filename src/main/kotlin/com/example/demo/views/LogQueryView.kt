package com.example.demo.views

import com.example.demo.entity.LogEntry
import com.example.demo.entity.LogLevel
import com.example.demo.service.LogQueryService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.datetimepicker.DateTimePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer
import com.vaadin.flow.router.Route
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 日志查询系统界面
 * 功能强大的日志搜索、过滤和展示系统
 */
@Route("logs")
class LogQueryView(
    private val logQueryService: LogQueryService
) : KComposite() {

    private lateinit var keywordField: TextField
    private lateinit var levelCheckboxGroup: CheckboxGroup<LogLevel>
    private lateinit var startTimePicker: DateTimePicker
    private lateinit var endTimePicker: DateTimePicker
    private lateinit var loggerSelect: Select<String>
    private lateinit var sourceSelect: Select<String>
    private lateinit var logGrid: Grid<LogEntry>
    private lateinit var resultCountSpan: Span
    
    private var currentLogs: List<LogEntry> = emptyList()

    private val root = ui {
        verticalLayout {
            setSizeFull()
            setPadding(true)
            
            // 标题栏
            horizontalLayout {
                setWidthFull()
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                
                h2("日志查询系统") {
                    style.set("margin", "0")
                }
                
                span {
                    style.set("margin-left", "auto")
                    style.set("color", "var(--lumo-secondary-text-color)")
                    text = "实时日志监控与分析"
                }
            }

            // 搜索和过滤区域
            div {
                addClassName("filter-panel")
                style.set("background", "var(--lumo-contrast-5pct)")
                style.set("padding", "var(--lumo-space-m)")
                style.set("border-radius", "var(--lumo-border-radius-m)")
                
                verticalLayout {
                    isPadding = false
                    
                    // 第一行：关键词搜索
                    horizontalLayout {
                        setWidthFull()
                        defaultVerticalComponentAlignment = FlexComponent.Alignment.END
                        
                        keywordField = textField("关键词搜索") {
                            placeholder = "搜索日志消息或 Logger 名称..."
                            width = "400px"
                            prefixComponent = VaadinIcon.SEARCH.create()
                            isClearButtonVisible = true
                        }
                        
                        button("搜索") {
                            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                            icon = VaadinIcon.SEARCH.create()
                            onLeftClick { performSearch() }
                        }
                        
                        button("重置") {
                            addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                            icon = VaadinIcon.REFRESH.create()
                            onLeftClick { resetFilters() }
                        }
                        
                        button("导出") {
                            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                            icon = VaadinIcon.DOWNLOAD.create()
                            onLeftClick { exportLogs() }
                        }
                    }
                    
                    // 第二行：高级过滤器
                    horizontalLayout {
                        setWidthFull()
                        defaultVerticalComponentAlignment = FlexComponent.Alignment.END
                        
                        // 时间范围
                        startTimePicker = dateTimePicker("开始时间") {
                            width = "220px"
                            value = LocalDateTime.now().minusHours(24)
                        }
                        
                        endTimePicker = dateTimePicker("结束时间") {
                            width = "220px"
                            value = LocalDateTime.now()
                        }
                        
                        // Logger 选择
                        loggerSelect = select<String> {
                            label = "Logger"
                            width = "250px"
                            setItems(listOf("全部") + logQueryService.getAvailableLoggers())
                            value = "全部"
                        }
                        
                        // 来源选择
                        sourceSelect = select<String> {
                            label = "来源"
                            width = "180px"
                            setItems(listOf("全部") + logQueryService.getAvailableSources())
                            value = "全部"
                        }
                    }
                    
                    // 第三行：日志级别选择
                    horizontalLayout {
                        setWidthFull()
                        
                        levelCheckboxGroup = checkBoxGroup<LogLevel> {
                            label = "日志级别"
                            setItems(LogLevel.entries)
                            setItemLabelGenerator { it.displayName }
                            value = LogLevel.entries.toSet()
                        }
                    }
                }
            }

            // 结果统计
            horizontalLayout {
                setWidthFull()
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                style.set("margin-top", "var(--lumo-space-m)")
                
                resultCountSpan = span {
                    style.set("font-weight", "500")
                }
                
                span {
                    style.set("margin-left", "auto")
                    style.set("color", "var(--lumo-secondary-text-color)")
                    style.set("font-size", "var(--lumo-font-size-s)")
                    text = "提示：双击行查看详细信息"
                }
            }

            // 日志表格
            logGrid = grid<LogEntry> {
                setSizeFull()
                addThemeVariants(
                    GridVariant.LUMO_ROW_STRIPES,
                    GridVariant.LUMO_COMPACT,
                    GridVariant.LUMO_WRAP_CELL_CONTENT
                )
                
                // 时间列
                addColumn { log ->
                    log.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                }.apply {
                    setHeader("时间")
                    width = "180px"
                    isSortable = true
                    setKey("timestamp")
                }
                
                // 级别列（带颜色标签）
                addColumn(ComponentRenderer<Span, LogEntry> { log ->
                    Span().apply {
                        text = log.level.displayName
                        element.style.apply {
                            set("background-color", log.level.color)
                            set("color", "white")
                            set("padding", "4px 8px")
                            set("border-radius", "4px")
                            set("font-weight", "500")
                            set("font-size", "0.8em")
                        }
                    }
                }).apply {
                    setHeader("级别")
                    width = "100px"
                    isSortable = true
                    setKey("level")
                }
                
                // Logger 列
                addColumn { it.logger }.apply {
                    setHeader("Logger")
                    width = "300px"
                    isSortable = true
                    setKey("logger")
                }
                
                // 消息列
                addColumn(ComponentRenderer<Div, LogEntry> { log ->
                    Div().apply {
                        style.set("white-space", "normal")
                        text = log.message
                        if (log.exception != null) {
                            add(Span(" ⚠").apply {
                                style.set("color", "var(--lumo-error-color)")
                                element.setAttribute("title", "包含异常信息")
                            })
                        }
                    }
                }).apply {
                    setHeader("消息")
                    isAutoWidth = true
                    flexGrow = 1
                }
                
                // 来源列
                addColumn { it.source ?: "-" }.apply {
                    setHeader("来源")
                    width = "150px"
                    isSortable = true
                    setKey("source")
                }
                
                // 标签列
                addColumn(ComponentRenderer<Div, LogEntry> { log ->
                    Div().apply {
                        log.tags.forEach { tag ->
                            add(Span(tag).apply {
                                element.style.apply {
                                    set("background-color", "var(--lumo-contrast-10pct)")
                                    set("padding", "2px 6px")
                                    set("border-radius", "3px")
                                    set("font-size", "0.75em")
                                    set("margin-right", "4px")
                                }
                            })
                        }
                    }
                }).apply {
                    setHeader("标签")
                    width = "200px"
                }
                
                // 双击查看详情
                addItemDoubleClickListener { event ->
                    showLogDetails(event.item)
                }
            }
        }
    }

    init {
        // 初始加载数据
        performSearch()
    }

    private fun performSearch() {
        val keyword = keywordField.value?.takeIf { it.isNotBlank() }
        val levels = levelCheckboxGroup.value
        val startTime = startTimePicker.value
        val endTime = endTimePicker.value
        val logger = loggerSelect.value?.takeIf { it != "全部" }
        val source = sourceSelect.value?.takeIf { it != "全部" }
        
        currentLogs = logQueryService.queryLogs(
            keyword = keyword,
            levels = levels,
            startTime = startTime,
            endTime = endTime,
            logger = logger,
            source = source,
            limit = 500
        )
        
        logGrid.setItems(currentLogs)
        resultCountSpan.text = "找到 ${currentLogs.size} 条日志记录"
    }

    private fun resetFilters() {
        keywordField.clear()
        levelCheckboxGroup.value = LogLevel.entries.toSet()
        startTimePicker.value = LocalDateTime.now().minusHours(24)
        endTimePicker.value = LocalDateTime.now()
        loggerSelect.value = "全部"
        sourceSelect.value = "全部"
        performSearch()
    }

    private fun exportLogs() {
        // 模拟导出功能
        com.vaadin.flow.component.notification.Notification.show(
            "导出 ${currentLogs.size} 条日志记录（功能演示）",
            3000,
            com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER
        )
    }

    private fun showLogDetails(log: LogEntry) {
        Dialog().apply {
            width = "800px"
            
            add(verticalLayout {
                isPadding = false
                
                h2("日志详情") {
                    style.set("margin-top", "0")
                }
                
                // 基本信息
                formLayout {
                    setResponsiveSteps(
                        com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 2)
                    )
                    
                    textField("ID") {
                        value = log.id
                        isReadOnly = true
                    }
                    
                    textField("时间") {
                        value = log.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                        isReadOnly = true
                    }
                    
                    textField("级别") {
                        value = log.level.displayName
                        isReadOnly = true
                        style.set("color", log.level.color)
                        style.set("font-weight", "bold")
                    }
                    
                    textField("线程") {
                        value = log.thread ?: "-"
                        isReadOnly = true
                    }
                    
                    textField("Logger") {
                        value = log.logger
                        isReadOnly = true
                        setColspan(this, 2)
                    }
                    
                    textField("来源") {
                        value = log.source ?: "-"
                        isReadOnly = true
                    }
                    
                    textField("标签") {
                        value = log.tags.joinToString(", ")
                        isReadOnly = true
                    }
                }
                
                // 消息内容
                textArea("消息") {
                    value = log.message
                    isReadOnly = true
                    width = "100%"
                    height = "100px"
                }
                
                // 异常信息
                if (log.exception != null) {
                    textArea("异常堆栈") {
                        value = log.exception
                        isReadOnly = true
                        width = "100%"
                        height = "150px"
                        style.set("font-family", "monospace")
                        style.set("font-size", "0.9em")
                    }
                }
                
                // 元数据
                if (log.metadata.isNotEmpty()) {
                    details("元数据") {
                        formLayout {
                            log.metadata.forEach { (key, value) ->
                                textField(key) {
                                    this.value = value
                                    isReadOnly = true
                                }
                            }
                        }
                    }
                }
                
                // 关闭按钮
                horizontalLayout {
                    style.set("justify-content", "flex-end")
                    style.set("width", "100%")
                    
                    button("关闭") {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        onLeftClick { this@apply.close() }
                    }
                }
            })
            
            open()
        }
    }
}
