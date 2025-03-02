package chatbot.dsl

import chatbot.api.Keyboard

@ChatBotDsl
class RowDsl {
    val buttons = mutableListOf<Keyboard.Button>()

    fun button(text: String) {
        buttons.add(Keyboard.Button(text))
    }

    operator fun String.unaryMinus() {
        buttons.add(Keyboard.Button(this))
    }
}
