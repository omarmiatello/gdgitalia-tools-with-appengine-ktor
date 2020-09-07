package com.github.omarmiatello.gdgtools.notification

import com.github.omarmiatello.gdgtools.appengine.SlackApi
import com.github.omarmiatello.gdgtools.data.FireDB
import com.github.omarmiatello.gdgtools.data.MessageStatus

class SlackHelper(
    val chatId: String,
    val type: String,
    val maxNew: Int = 10,
    val maxUpdate: Int = 10,
    val maxDelete: Int = 10
) {
    val slackNotification = FireDB.getSlackMessageStatusByType(chatId, type)
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
                        val messageId = SlackApi.sendMessage(
                            text = text,
                        ).isSuccessStatusCode
                        response.appendLine(
                            "NEW $chatId/$type/$slug - messageId: $messageId > ${
                                text.replace('\n', ' ').take(140)
                            }..."
                        )
                        saveMessageStatus(slug, MessageStatus(0, toSlackHash(text)))
                    }
                }
                messageStatus.hash != toSlackHash(text) -> {
//                    if (countUpdate++ < maxUpdate) {
//                        val messageId = SlackApi.editMessageText(
//                            text = text,
//                        ).result.message_id
//                        response.appendLine(
//                            "UPDATE $chatId/$type/$slug - messageId: $messageId > ${
//                                text.replace('\n', ' ').take(140)
//                            }..."
//                        )
//                        saveMessageStatus(slug, MessageStatus(messageId, toSlackHash(text)))
//                    }
                }
            }
        }
    }

    fun getMessageStatus(slug: String) = slackNotification[slug]

    fun saveMessageStatus(slug: String, messageStatus: MessageStatus) {
        FireDB.addSlackMessageStatus(
            chatId,
            type,
            slug,
            messageStatus
        )
    }

    data class Message(val slug: String, val text: String)

    companion object {
        val TYPE_EVENT = "event"
        val TYPE_SLIDE = "slide"
        val TYPE_NEXTWEEK = "nextweek"
        val TYPE_NEXTWEEK_HELPER = "nextweek_helper"
        val TYPE_FORWARD = "forward"

        fun toSlackHash(text: String) = text.hashCode().toString(16)
    }
}