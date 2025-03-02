package chatbot.dsl

import chatbot.api.ChatContext
import chatbot.api.Message
import chatbot.bot.MessageHandler
import chatbot.bot.MessageProcessor

@ChatBotDsl
open class ContextBehaviourDsl<C : ChatContext?> {
    val handlers = mutableListOf<MessageHandler<C>>()

    fun onMessage(predicate: (Message) -> Boolean = { true }, processor: MessageProcessor<C>) {
        val messagePredicate: (Message, C) -> Boolean = { message, _ -> predicate(message) }
        handlers.add(MessageHandler(messagePredicate, processor))
    }

    fun onCommand(command: String, processor: MessageProcessor<C>) {
        onMessage({ message -> message.text.startsWith("/$command") }, processor)
    }

    fun onMessage(text: String, processor: MessageProcessor<C>) {
        onMessage({ message -> message.text == text }, processor)
    }

    fun onMessageContains(text: String, processor: MessageProcessor<C>) {
        onMessage({ message -> message.text.contains(text) }, processor)
    }

    fun onMessagePrefix(text: String, processor: MessageProcessor<C>) {
        onMessage({ message -> message.text.startsWith(text) }, processor)
    }
}
