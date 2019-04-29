package com.github.omarmiatello.gdgtools.data

import com.github.omarmiatello.gdgtools.appengine.FirebaseDatabaseApi
import com.github.omarmiatello.gdgtools.appengine.fireMap
import com.github.omarmiatello.gdgtools.utils.yearInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import java.util.*

object FireDB : FirebaseDatabaseApi() {
    override val basePath = "https://gdg-italia.firebaseio.com/"

    private val KEY_GROUP = "group"
    private val KEY_GROUP_MEETUP_URLNAME = "group-meetup-urlname"
    private val KEY_EVENT = "event"
    private val KEY_SPEAKER = "speaker"
    private val KEY_SLIDE = "slide"
    private val KEY_TAG = "tag"
    private val KEY_TELEGRAM_MESSAGE = "telegram-message"

    private val SERIALIZER_STRING_STRING = (String.serializer() to String.serializer()).map
    private val SERIALIZER_SLUG_GROUP = (String.serializer() to GroupDao.serializer()).map
    private val SERIALIZER_SLUG_EVENT = (String.serializer() to EventDao.serializer()).map
    private val SERIALIZER_SLUG_SPEAKER = (String.serializer() to SpeakerDao.serializer()).map
    private val SERIALIZER_SLUG_SLIDE = (String.serializer() to SlideDao.serializer()).map
    private val SERIALIZER_SLUG_TAG = (String.serializer() to TagDao.serializer()).map
    private val SERIALIZER_SLUG_TELEGRAMSTATUS = (String.serializer() to MessageStatus.serializer()).map
    private val SERIALIZER_TYPE_SLUG_TELEGRAMSTATUS =
        (String.serializer() to SERIALIZER_SLUG_TELEGRAMSTATUS).map
    private val SERIALIZER_TAG_BY_YEAR_WITH_COUNTER = (String.serializer() to (String.serializer() to Int.serializer()).map).map

    var groupsMap by fireMap(KEY_GROUP, SERIALIZER_SLUG_GROUP)
    var meetupUrlnamesMap by fireMap(KEY_GROUP_MEETUP_URLNAME, SERIALIZER_STRING_STRING)
    var eventsMap by fireMap(KEY_EVENT, (String.serializer() to SERIALIZER_SLUG_EVENT).map)
    var speakersMap by fireMap(KEY_SPEAKER, SERIALIZER_SLUG_SPEAKER)
    var slidesMap by fireMap(KEY_SLIDE, (String.serializer() to SERIALIZER_SLUG_SLIDE).map)
    var tagsMap by fireMap(KEY_TAG, SERIALIZER_SLUG_TAG)

    // group

    var allGroups: List<GroupDao>
        get() = groupsMap.values.toList()
        set(value) {
            groupsMap = value.sortedBy { it.slug }.associateBy { it.slug }
        }

    fun getGroup(groupSlug: String): GroupDao? = this["$KEY_GROUP/$groupSlug", GroupDao.serializer()]

    fun addGroupTags(groupSlug: String, tags: YearTagCounter) {
        this["$KEY_GROUP/$groupSlug/tags", SERIALIZER_TAG_BY_YEAR_WITH_COUNTER] = tags
    }

    // event

    val allEvents: List<EventDao>
        get() = eventsMap.values.flatMap { it.values }
            .sortedBy { it.time }

    fun addEvents(events: List<EventDao>) = update(
        FireDB.KEY_EVENT,
        events
            .groupBy { it.groupSlug }
            .mapValues { it.value.associateBy { it.slug } },
        SERIALIZER_SLUG_EVENT
    )

    fun getEventsBy(groupSlug: String): Map<String, EventDao> =
        this["$KEY_EVENT/$groupSlug", SERIALIZER_SLUG_EVENT].orEmpty()

    fun getAllEventWithTag(tag: String): Map<String, List<EventDao>> = eventsMap
        .flatMap { it.value.map { it.value }.filter { tag in it.tags.orEmpty() } }
        .sortedByDescending { it.time }
        .groupBy { Date(it.time).yearInt.toString() }

    fun getAllEventByYear(year: Int) = allEvents
        .filter { Date(it.time).yearInt == year }

    // speaker

    fun addSpeaker(speaker: SpeakerDao) {
        this["$KEY_SPEAKER/${speaker.slug}", SpeakerDao.serializer()] = speaker
    }

    fun addSpeakerTags(speakerSlug: String, tags: YearTagCounter) {
        this["$KEY_SPEAKER/$speakerSlug/tags", SERIALIZER_TAG_BY_YEAR_WITH_COUNTER] = tags
    }

    fun addSpeakers(speaker: List<SpeakerDao>) = update(
        FireDB.KEY_SPEAKER,
        speaker.associateBy { it.slug },
        SpeakerDao.serializer()
    )

    fun getSpeaker(speakerSlug: String): SpeakerDao? = this["$KEY_SPEAKER/$speakerSlug", SpeakerDao.serializer()]

    // slide

    fun addSlide(slide: SlideDao) {
        this["$KEY_SLIDE/${slide.speakerSlug}/${slide.slug}", SlideDao.serializer()] = slide
    }

    fun addSlides(slides: List<SlideDao>) = slides
        .groupBy { it.speakerSlug }
        .forEach { speakerSlug, speakerSlides ->
            update(
                FireDB.KEY_SLIDE + "/$speakerSlug",
                speakerSlides.associateBy { it.slug },
                SlideDao.serializer()
            )
        }

    fun getSlidesBy(speakerSlug: String): Map<String, SlideDao> =
        this["$KEY_SLIDE/$speakerSlug", SERIALIZER_SLUG_SLIDE].orEmpty()

    // tag

    fun getTag(tagSlug: String): TagDao? = this["$KEY_TAG/$tagSlug", TagDao.serializer()]

    // telegram message (status)

    fun addTelegramMessageStatus(chatId: String, type: String, slug: String, status: MessageStatus) {
        this["$KEY_TELEGRAM_MESSAGE/$chatId/$type/$slug", MessageStatus.serializer()] = status
    }

    fun addTelegramMessagesStatus(
        chatId: String,
        type: String,
        slugsAndStatus: Map<String, MessageStatus>
    ) {
        update("$KEY_TELEGRAM_MESSAGE/$chatId/$type", slugsAndStatus, MessageStatus.serializer())
    }

    fun getTelegramMessageStatusByChat(chatId: String): Map<String, Map<String, MessageStatus>> =
        this["$KEY_TELEGRAM_MESSAGE/$chatId", SERIALIZER_TYPE_SLUG_TELEGRAMSTATUS].orEmpty()

    fun getTelegramMessageStatusByType(chatId: String, type: String):  Map<String, MessageStatus> =
        this["$KEY_TELEGRAM_MESSAGE/$chatId/$type", SERIALIZER_SLUG_TELEGRAMSTATUS].orEmpty()

    fun deleteTelegramMessageStatus(chatId: String, type: String, slug: String) =
        deletePath("$KEY_TELEGRAM_MESSAGE/$chatId/$type/$slug")

}

@Serializable
data class MessageStatus(val id: Int, val hash: String)