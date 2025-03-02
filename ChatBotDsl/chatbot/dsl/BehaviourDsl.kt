package chatbot.dsl

import chatbot.api.*
import chatbot.bot.MessageHandler
import chatbot.bot.MessageProcessorContext

@ChatBotDsl
class BehaviourDsl : ContextBehaviourDsl<ChatContext?>() {
    inline fun <reified C : ChatContext> into(noinline block: ContextBehaviourDsl<C>.() -> Unit) {
        val contextBehaviourDsl = ContextBehaviourDsl<C>().apply(block)

        contextBehaviourDsl.handlers.forEach { handler ->
            val newHandler = MessageHandler<ChatContext?>(
                predicate = { message, context ->
                    context is C && handler.predicate(message, context)
                },
                processor = {
                    if (this.context is C) {
                        handler.processor(
                            MessageProcessorContext(
                                message = this.message,
                                client = this.client,
                                context = this.context,
                                setContext = this.setContext,
                            ),
                        )
                    }
                },
            )
            handlers.add(newHandler)
        }
    }

    inline infix fun <reified C : ChatContext> C.into(block: ContextBehaviourDsl<C>.() -> Unit) {
        val contextBehaviourDsl = ContextBehaviourDsl<C>().apply(block)

        contextBehaviourDsl.handlers.forEach { handler ->
            val newHandler = MessageHandler<ChatContext?>(
                predicate = { message, context ->
                    context is C && context == this && handler.predicate(message, context)
                },
                processor = {
                    if (this.context is C && this.context == this@into) {
                        handler.processor(
                            MessageProcessorContext(
                                message = this.message,
                                client = this.client,
                                context = this.context,
                                setContext = this.setContext,
                            ),
                        )
                    }
                },
            )
            handlers.add(newHandler)
        }
    }
}
