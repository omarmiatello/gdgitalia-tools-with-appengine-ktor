package com.github.omarmiatello.gdgtools

import com.github.omarmiatello.gdgtools.config.AppConfig
import com.github.omarmiatello.gdgtools.data.*
import com.github.omarmiatello.gdgtools.utils.*
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import notification.TelegramHelper
import java.util.*
import kotlin.math.max

private fun EventDao.telegramMessage(group: GroupDao, skipTags: List<Tag> = emptyList()): String {
    val skipLinks = skipTags.map { it.telegramLink }
    val channels = getTagList().orEmpty()
        .filter { it.telegramLink != null && it.telegramLink !in skipLinks }
        .map { "[${it.channelName}](${it.telegramLink})" }
        .distinct()
        .joinToString(", ")
    val venue = if (!venueName.isNullOrEmpty()) {
        val address = listOfNotNull(venueAddress, venueCity).joinToString(", ")
        if (address.isNotEmpty()) "*$venueName* ($address)" else "*$venueName*"
    } else null
    val tag = tags?.takeIf { it.isNotEmpty() }
    val channel = if (channels.isNotEmpty()) "Canali: $channels" else null
    val footer = listOfNotNull(venue, tag, channel).joinToString("\n|").let { if (it.isNotEmpty()) "\n|$it" else it }
    return """${group.name}: [🎟 $name]($meetupLink)
            |*$dateString* dalle $timeString$footer""".trimMargin()
//                            |${description.orEmpty().htmlToTelegramMarkdown()}
}

private fun SlideDao.telegramMessage(speaker: SpeakerDao, skipChannels: List<Tag> = emptyList()): String {
    val skipLinks = skipChannels.map { it.telegramLink }
    val channels = getTagList().orEmpty()
        .filter { it.telegramLink != null && it.telegramLink !in skipLinks }
        .map { "[${it.channelName}](${it.telegramLink})" }
        .distinct()
        .joinToString(", ")
    val tag = tags?.takeIf { it.isNotEmpty() }
    val channel = if (channels.isNotEmpty()) "Canali: $channels" else null
    val footer = listOfNotNull(tag, channel).joinToString("\n|").let { if (it.isNotEmpty()) "\n|$it" else it }
    return "${speaker.name}: [🎤 $name]($link)$footer".trimMargin()
//                            |${description.orEmpty().htmlToTelegramMarkdown()}
}

fun Routing.notification() {
    val telegramConfig = AppConfig.getDefault().telegram

    route("notification") {
        get("telegram/bychannel") {
            val groupsMap = FireDB.groupsMap
            val eventsMap = FireDB.eventsMap
            val speakersMap = FireDB.speakersMap
            val slidesMap = FireDB.slidesMap
            val tagsMap = FireDB.tagsMap

            val tagsByChannelId = knownTags
                .filterNot { it.channelId.isNullOrEmpty() }
                .groupBy { it.channelId!! }


            val telegrams = tagsByChannelId.map { (chatId, tags) ->
                var canSendMore = true

                listOf(
                    TelegramHelper(chatId, TelegramHelper.TYPE_EVENT).apply {
                        val events = tags
                            .flatMap { tagsMap[it.slug]?.event.orEmpty().toList() }
                            .flatMap { (key1, v) -> v.map { key2 -> key1 to key2 } }
                            .distinct()
                            .sortedBy { it.second }

                        val msgs = events.map { (key1, key2) ->
                            val event = eventsMap.getValue(key1).getValue(key2)
                            val group = groupsMap.getValue(key1)
                            TelegramHelper.Message(
                                slug = "${key1}_$key2",
                                text = event.telegramMessage(group, skipTags = tags)
                            )
                        }

                        if (canSendMore) {
                            send(msgs)
                            canSendMore = response.isEmpty()
                        }
                        deleteIfNotIn(msgs)
                        // deleteIf { t -> events.firstOrNull { it.slug == t.second && it.groupSlug == it.groupSlug } == null }
                    },
                    TelegramHelper(chatId, TelegramHelper.TYPE_SLIDE).apply {
                        val slides = tags
                            .flatMap { tagsMap[it.slug]?.slide.orEmpty().toList() }
                            .flatMap { (key1, v) -> v.map { key2 -> key1 to key2 } }
                            .distinct()

                        val msgs = slides.map { (key1, key2) ->
                            TelegramHelper.Message(
                                slug = "${key1}_$key2",
                                text = slidesMap[key1]!![key2]!!.telegramMessage(
                                    speakersMap[key1]!!,
                                    skipChannels = tags
                                )
                            )
                        }

                        if (canSendMore) {
                            send(msgs)
                            canSendMore = response.isEmpty()
                        }
                        deleteIfNotIn(msgs)
                        // deleteIf { t -> events.firstOrNull { it.slug == t.second && it.groupSlug == it.groupSlug } == null }
                    }
                )
            }

            val showResult = call.parameters["show"] == "1"
            call.respondText { if (showResult) telegrams.flatten().joinToString("\n") { it.response } else "OK" }
        }

        get("telegram/nextweek") {
            val chatId = telegramConfig.chatId_all_events
            val groupsMap = FireDB.groupsMap
            val eventsMap = FireDB.eventsMap

            val (start, end) = weekRangeFrom { add(Calendar.HOUR, 36) }

            val events = eventsMap.values
                .flatMap { it.values }
                .filter { Date(it.time).after(start.time) }
                .sortedBy { it.time }

            val (nextEvents, futureEvents) = events.partition { Date(it.time).before(end.time) }

            val nextMsg = if (!nextEvents.isNullOrEmpty()) {
                "Week ${end.weekOfYear} (${start.time.formatFull()} - ${end.time.formatFull()})\n" +
                        nextEvents.joinToString("\n\n") { it.telegramMessage(groupsMap[it.groupSlug]!!) }
            } else null

            val futureHelper = TelegramHelper(chatId, TelegramHelper.TYPE_NEXTWEEK_HELPER)
            val futureStatus = futureHelper.getMessageStatus("future")

            val maxShowFutureEvents = max(
                5,
                futureEvents.getOrNull(4)?.let {
                    val lastDate = it.dateString
                    futureEvents.indexOfLast { it.dateString == lastDate } + 1
                } ?: 0
            )
            val futureMsg = if (!futureEvents.isNullOrEmpty()) {
                ("Prossimamente:\n" +
                        futureEvents.take(maxShowFutureEvents)
                            .joinToString("\n") {
                                val group = groupsMap[it.groupSlug]!!
                                val date = Date(it.time)
                                "*${date.formatDayMonth()}* ${group.name}: [🎟 ${it.name}](${it.meetupLink})"
                            } +
                        (futureEvents
                            .drop(maxShowFutureEvents)
                            .takeIf { it.isNotEmpty() }
                            ?.let { "\n+ altri ${it.count()} eventi (da: ${it.map { groupsMap[it.groupSlug]!!.name }.distinct().joinToString()})" }
                            ?: "")
                        )
                    .takeIf { nextMsg != null || TelegramHelper.toTelegramHash(it) != futureStatus?.hash }
            } else null

            val text = listOfNotNull(nextMsg, futureMsg).joinToString("\n\n")

            if (text.isNotEmpty()) {
                val msg = TelegramHelper.Message("${end.year}-week${end.weekOfYear}", text)

                if (futureMsg != null) {
                    futureHelper.saveMessageStatus("future", MessageStatus(0, TelegramHelper.toTelegramHash(futureMsg)))
                }

                val telegram = TelegramHelper(chatId, TelegramHelper.TYPE_NEXTWEEK).apply { send(listOf(msg)) }

                val telegrams = listOfNotNull(
                    telegram
//                    ,
//                    // forward message // Removed for a new feature
//                    telegram.idsMsgSends.firstOrNull()?.let { msgId ->
//                        TelegramHelper(MyConfig.CHAT_gdgitalia, TelegramHelper.TYPE_FORWARD)
//                            .apply { forward(listOf(chatId to msgId)) }
//                    }
                )

                val showResult = call.parameters["show"] == "1"
                call.respondText { if (showResult) telegrams.joinToString("\n") { it.response } else "OK" }
            } else {
                call.respondText("OK")
            }
        }
    }
}