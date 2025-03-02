package chatbot.dsl

import chatbot.api.ChatBot
import chatbot.api.ChatId
import chatbot.api.Client
import chatbot.bot.Bot
import chatbot.bot.MessageProcessorContext

@Target(AnnotationTarget.CLASS)
@DslMarker
annotation class ChatBotDsl

fun chatBot(client: Client, block: BotConfigDsl.() -> Unit = {}): ChatBot {
    val config = BotConfigDsl().apply(block)

    return Bot(
        logLevel = config.logLevel,
        messageHandlers = config.messageHandlers,
        contextManager = config.context,
        client = client,
    )
}

fun MessageProcessorContext<*>.sendMessage(chatId: ChatId, msg: String = "", block: SendMessageDsl.() -> Unit = {}) {
    val messageDsl = SendMessageDsl(chatId, client, message).apply {
        if (msg.isNotBlank()) {
            text = msg
        }
        apply(block)
    }
    messageDsl.send()
}
