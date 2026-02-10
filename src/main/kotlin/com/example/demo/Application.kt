package com.example.demo

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
@PWA(
    name = "Vaadin Demo",
    shortName = "Demo",
    themeColor = "#233348"
)
@Push
class Application : AppShellConfigurator

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
