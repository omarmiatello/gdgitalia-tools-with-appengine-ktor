package notification

import com.github.omarmiatello.gdgtools.appengine.TelegramApi
import com.github.omarmiatello.gdgtools.data.FireDB
import com.github.omarmiatello.gdgtools.data.MessageStatus
import com.google.api.client.http.HttpResponseException

class TelegramHelper(val chatId: String, val type: String, val maxNew: Int = 10, val maxUpdate: Int = 10, val maxDelete: Int = 10) {
    val telegramNotification = FireDB.getTelegramMessageStatusByType(chatId, type)
    val response: StringBuilder = StringBuilder()
    var idsMsgSends = emptyList<Int>()
        private set

    fun send(msgs: List<Message>) {
        var countNew = 0
        var countUpdate = 0
        msgs.forEach { (slug, text) ->
            val messageStatus = getMessageStatus(slug)
            if (messageStatus != null) idsMsgSends += messageStatus.id
            when {
                messageStatus == null -> {
                    if (countNew++ < maxNew) {
                        val messageId = TelegramApi.sendMessage(
                            chatId = chatId,
                            text = text,
                            parseMode = TelegramApi.ParseMode.MARKDOWN,
                            disableWebPagePreview = true
                            //button = listOf(listOf(InlineKeyboardButton("Meetup", event.meetupLink!!)))
                        ).result.message_id
                        idsMsgSends += messageId
                        response.appendln("NEW $chatId/$type/$slug - messageId: $messageId > ${text.replace('\n', ' ').take(140)}...")
                        saveMessageStatus(slug, MessageStatus(messageId, toTelegramHash(text)))
                    }
                }
                messageStatus.hash != toTelegramHash(text) -> {
                    if (countUpdate++ < maxUpdate) {
                        val messageId = TelegramApi.editMessageText(
                            chatId = chatId,
                            messageId = messageStatus.id,
                            text = text,
                            parseMode = TelegramApi.ParseMode.MARKDOWN,
                            disableWebPagePreview = true
                            //button = listOf(listOf(InlineKeyboardButton("Meetup", event.meetupLink!!)))
                        ).result.message_id
                        response.appendln("UPDATE $chatId/$type/$slug - messageId: $messageId > ${text.replace('\n', ' ').take(140)}...")
                        saveMessageStatus(slug, MessageStatus(messageId, toTelegramHash(text)))
                    }
                }
            }
        }
    }

    fun forward(fromChatMsgId: List<Pair<String, Int>>) {
        var countNew = 0
        fromChatMsgId.forEach { (fromChatId, fromMessageId) ->
            val slug = "$fromChatId-$fromMessageId"
            val messageStatus = getMessageStatus(slug)
            if (messageStatus == null) {
                if (countNew++ < maxNew) {
                    val messageId = TelegramApi.forwardMessage(
                        chatId = chatId,
                        fromChatId = fromChatId,
                        messageId = fromMessageId
                    ).result.message_id
                    response.appendln("FORWARD $chatId/$type/$slug - messageId: $messageId")
                    saveMessageStatus(slug, MessageStatus(messageId, "forward"))
                }
            }
        }
    }

    fun deleteIf(skipIfError: Boolean = true, predicate: (Map.Entry<String, MessageStatus>) -> Boolean) {
        var countDeleted = 0
        val deletedNotification = telegramNotification.filter(predicate)
        deletedNotification.forEach { (slug, messageStatus) ->
            if (messageStatus.hash != "deleted" && countDeleted++ < maxDelete) {
                var isDeleted = true
                try {
                    val status = TelegramApi.deleteMessage(chatId, messageStatus.id).result
                    response.appendln("DELETED (deleteMessage) [$status] $slug")
                } catch (e: HttpResponseException) {
                    try {
                        // BOT limit: After 48h message can't be deleted
                        // {"ok":false,"error_code":400,"description":"Bad Request: message can't be deleted"}
                        val messageId = TelegramApi.editMessageText(
                            chatId = chatId, messageId = messageStatus.id,
                            text = "Evento spostato o eliminato"
                        ).result.message_id
                        response.appendln("DELETED (editMessageText) [$messageId] $slug")
                    } catch (e: HttpResponseException) {
                        // message remove from other admin (or bot)
                        isDeleted = skipIfError
                        if (isDeleted) {
                            response.appendln("DELETED (editMessageText) [already deleted] $slug")
                        }
                    }
                }
                if (isDeleted) {
                    saveMessageStatus(slug, MessageStatus(messageStatus.id, "deleted"))
                }
            }
        }
    }

    fun getMessageStatus(slug: String) = telegramNotification[slug]

    fun saveMessageStatus(slug: String, messageStatus: MessageStatus) {
        FireDB.addTelegramMessageStatus(
            chatId,
            type,
            slug,
            messageStatus
        )
    }

    fun deleteIfNotIn(msgs: List<Message>) {
        val slugs = msgs.map { it.slug }
        deleteIf { it.key !in slugs }
    }

    data class Message(val slug: String, val text: String)

    companion object {
        val TYPE_EVENT = "event"
        val TYPE_SLIDE = "slide"
        val TYPE_NEXTWEEK = "nextweek"
        val TYPE_NEXTWEEK_HELPER = "nextweek_helper"
        val TYPE_FORWARD = "forward"

        fun toTelegramHash(text: String) = text.hashCode().toString(16)
    }
}