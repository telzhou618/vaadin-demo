package com.example.demo.service

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.notification.Notification
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class BroadcastService {
    private val uis = ConcurrentHashMap.newKeySet<UI>()

    fun register(ui: UI) {
        uis.add(ui)
    }

    fun unregister(ui: UI) {
        uis.remove(ui)
    }

    fun broadcast(message: String) {
        uis.forEach { ui ->
            ui.access {
                Notification.show(
                    message,
                    5000,
                    Notification.Position.TOP_CENTER
                )
            }
        }
    }
}
