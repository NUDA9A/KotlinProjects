package chatbot.dsl

import chatbot.api.Keyboard

@ChatBotDsl
class KeyboardDsl() {
    var oneTime: Boolean = false
    var keyboard: MutableList<MutableList<Keyboard.Button>> = mutableListOf()

    fun row(block: RowDsl.() -> Unit) {
        val rowDsl = RowDsl().apply(block)
        keyboard.add(rowDsl.buttons)
    }
}
