package com.example.demo.components

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.textfield.TextField

object EmojiPicker {
    
    private val emojis = listOf(
        "😊", "😂", "❤️", "👍", "👎", "🙏", "😭", "😍", "🤔", "😅",
        "🎉", "✨", "🔥", "💯", "👏", "🤝", "💪", "🙌", "👌", "✅"
    )
    
    fun show(ui: UI, textField: TextField, position: Position = Position.CENTER) {
        val emojisJson = emojis.joinToString(",") { "\"$it\"" }
        val positionStyle = when (position) {
            Position.CENTER -> "bottom:120px;left:50%;transform:translateX(-50%);"
            Position.RIGHT -> "bottom:120px;right:50px;"
        }
        
        ui.page.executeJs(
            """
            const emojis = [$emojisJson];
            const picker = document.createElement('div');
            picker.style.cssText = 'position:fixed;$positionStyle;background:white;padding:12px;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.15);z-index:1000;display:grid;grid-template-columns:repeat(10,1fr);gap:8px;max-width:500px;';
            
            let isRemoved = false;
            const removePicker = () => {
                if (!isRemoved && picker.parentNode) {
                    isRemoved = true;
                    document.body.removeChild(picker);
                }
            };
            
            emojis.forEach(emoji => {
                const btn = document.createElement('button');
                btn.textContent = emoji;
                btn.style.cssText = 'font-size:24px;border:none;background:none;cursor:pointer;padding:4px;border-radius:6px;transition:background 0.2s;';
                btn.onmouseover = () => btn.style.background = '#f0f0f0';
                btn.onmouseout = () => btn.style.background = 'none';
                btn.onclick = () => {
                    removePicker();
                    return emoji;
                };
                picker.appendChild(btn);
            });
            
            const closeBtn = document.createElement('button');
            closeBtn.textContent = '✕';
            closeBtn.style.cssText = 'position:absolute;top:4px;right:4px;border:none;background:none;cursor:pointer;font-size:16px;color:#999;';
            closeBtn.onclick = () => {
                removePicker();
                return null;
            };
            picker.appendChild(closeBtn);
            
            document.body.appendChild(picker);
            
            setTimeout(() => {
                const clickOutside = (e) => {
                    if (!picker.contains(e.target)) {
                        removePicker();
                        document.removeEventListener('click', clickOutside);
                    }
                };
                document.addEventListener('click', clickOutside);
            }, 100);
            
            return new Promise((resolve) => {
                picker.addEventListener('click', (e) => {
                    if (e.target.tagName === 'BUTTON' && e.target !== closeBtn) {
                        resolve(e.target.textContent);
                    }
                });
                closeBtn.addEventListener('click', () => resolve(null));
            });
            """
        ).then { result ->
            if (!result.asString().isNullOrEmpty()) {
                val emoji = result.asString()
                val currentValue = textField.value ?: ""
                textField.value = currentValue + emoji
            }
        }
    }
    
    enum class Position {
        CENTER,
        RIGHT
    }
}
