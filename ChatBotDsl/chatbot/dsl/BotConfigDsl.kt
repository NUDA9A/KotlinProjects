package chatbot.dsl

import chatbot.api.ChatContext
import chatbot.api.ChatContextsManager
import chatbot.api.LogLevel
import chatbot.bot.MessageHandler

@ChatBotDsl
class BotConfigDsl {
    var logLevel: LogLevel = LogLevel.ERROR
    val messageHandlers = mutableListOf<MessageHandler<ChatContext?>>()
    var context: ChatContextsManager? = null

    fun use(contextManager: ChatContextsManager) {
        this.context = contextManager
    }

    fun use(logLevel: LogLevel) {
        this.logLevel = logLevel
    }

    operator fun LogLevel.unaryPlus() {
        logLevel = this
    }

    fun behaviour(block: BehaviourDsl.() -> Unit) {
        val behaviourDsl = BehaviourDsl().apply(block)
        messageHandlers.addAll(behaviourDsl.handlers)
    }
}
