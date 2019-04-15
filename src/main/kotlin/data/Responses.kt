package com.github.omarmiatello.gdgtools.data

import kotlinx.serialization.Serializable

// group

@Serializable
data class GroupResponse(
    val slug: String,
    val name: String,
    val membersCount: Int,
    val membersName: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val region: String?,
    val created: Long?,
    val city: String?,
    val country: String?,
    val meetupLink: String?,
    val logoImageUrl: String?,
    val backgroundImageUrl: String?,
    val tags: Map<String, List<TagCounterResponse>>?,
    val events: List<EventResponse>?
)

fun Collection<GroupDao>.toGroupResponseList() = map { it.toResponse(events = null) }

fun GroupDao.toResponse(events: List<EventResponse>?) = GroupResponse(
    slug,
    name,
    membersCount,
    membersName,
    description,
    lat,
    lon,
    region,
    created,
    city,
    country,
    meetupLink,
    logoImageUrl,
    backgroundImageUrl,
    tags?.mapValues {
        it.value
            .map { knownTagsBySlug[it.key]!!.toCounterResponse(it.value) }
            .sortedByDescending { it.count }
    },
    events
)

// event

@Serializable
data class EventResponse(
    val slug: String,
    val groupSlug: String,
    val name: String,
    val description: String?,
    val tags: List<TagBasicResponse>?,
    val dateString: String,
    val timeString: String,
    val created: Long,
    val duration: Long,
    val time: Long,
    val updated: Long,
    val attendeeRsvpYesCount: Int,
    val attendeeRsvpLimit: Int,
    val attendeeWaitlistCount: Int,
    val attendeeManualCount: Int,
    val venueName: String?,
    val venueLon: Double?,
    val venueLat: Double?,
    val venueCountry: String?,
    val venueCity: String?,
    val venueAddress: String?,
    val meetupLink: String?
)

fun Collection<EventDao>.toEventResponseList() = map { it.toResponse() }

fun EventDao.toResponse() = EventResponse(
    slug,
    groupSlug,
    name,
    description,
    getTagList()?.map { it.toBasicResponse() },
    dateString,
    timeString,
    created,
    duration,
    time,
    updated,
    attendeeRsvpYesCount,
    attendeeRsvpLimit,
    attendeeWaitlistCount,
    attendeeManualCount,
    venueName,
    venueLon,
    venueLat,
    venueCountry,
    venueCity,
    venueAddress,
    meetupLink
)

// speaker

@Serializable
data class SpeakerResponse(
    val name: String,
    val speakerDeckId: String?,
    val slideShareId: String?,
    val slug: String,
    val tags: Map<String, List<TagCounterResponse>>?,
    val slides: List<SlideResponse>?
)

fun Collection<SpeakerDao>.toSpeakerResponseList() = map { it.toResponse(slides = null) }

fun SpeakerDao.toResponse(slides: List<SlideResponse>?) = SpeakerResponse(
    name,
    speakerDeckId,
    slideShareId,
    slug,
    tags?.mapValues {
        it.value
            .map { knownTagsBySlug[it.key]!!.toCounterResponse(it.value) }
            .sortedByDescending { it.count }
    },
    slides
)

// slide

@Serializable
data class SlideResponse(
    val slug: String,
    val name: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val speakerSlug: String,
    val author: String,
    val image: String?,
    val tags: List<TagBasicResponse>?
)

fun Collection<SlideDao>.toSlideResponseList() = map { it.toResponse() }

fun SlideDao.toResponse() = SlideResponse(
    slug,
    name,
    description,
    pubDate,
    link,
    speakerSlug,
    author,
    image,
    getTagList()?.map { it.toBasicResponse() }
)

// tag

@Serializable
class TagFullResponse(
    val name: String,
    val hashtag: String,
    val slug: String,
    val group: Map<String, YearCounter>?,
    val event: Map<String, List<String>>?,
    val speaker: Map<String, YearCounter>?,
    val slide: Map<String, List<String>>?
)

fun Collection<TagDao>.toTagResponseList() = map { it.toFullResponse() }

fun TagDao.toFullResponse() = TagFullResponse(name, hashtag, slug, group, event, speaker, slide)

@Serializable
class TagBasicResponse(
    val name: String,
    val hashtag: String,
    val slug: String
)

@Serializable
class TagCounterResponse(
    val name: String,
    val hashtag: String,
    val slug: String,
    val count: Int
)

