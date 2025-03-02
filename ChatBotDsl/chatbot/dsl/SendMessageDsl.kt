package chatbot.dsl

import chatbot.api.*

@ChatBotDsl
class SendMessageDsl(val chatId: ChatId, val client: Client, val message: Message) {
    var text: String = ""
    var replyTo: MessageId? = null
    var keyboard: Keyboard? = null

    fun removeKeyboard() {
        keyboard = Keyboard.Remove
    }

    fun withKeyboard(block: KeyboardDsl.() -> Unit) {
        val keyboardDsl = KeyboardDsl().apply(block)
        keyboard = if (keyboardDsl.keyboard.isNotEmpty()) {
            val keyboardList: List<List<Keyboard.Button>> = keyboardDsl.keyboard.map { it.toList() }
            Keyboard.Markup(
                oneTime = keyboardDsl.oneTime,
                keyboard = keyboardList,
            )
        } else {
            null
        }
    }

    private fun isValidMessage(): Boolean {
        val hasText = text.isNotBlank()
        val hasKeyboard = keyboard != null &&
            (keyboard is Keyboard.Markup && (keyboard as Keyboard.Markup).keyboard.flatten().isNotEmpty())
        val isKeyboardRemove = keyboard is Keyboard.Remove
        return hasText || hasKeyboard || isKeyboardRemove
    }

    fun send() {
        if (isValidMessage()) {
            client.sendMessage(chatId, text, keyboard, replyTo)
        }
    }
}
