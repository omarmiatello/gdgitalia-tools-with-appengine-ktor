package com.github.omarmiatello.gdgtools.data

import com.github.omarmiatello.gdgtools.utils.toSlug
import kotlinx.serialization.Serializable

typealias YearCounter = Map<String, Int>

typealias YearTagCounter = Map<String, Map<String, Int>>

@Serializable
data class GroupDao(
    val slug: String,
    val name: String,
    val membersCount: Int,
    val membersName: String,
    val description: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val region: String? = null,
    val created: Long? = null,
    val city: String? = null,
    val country: String? = null,
    val meetupLink: String? = null,
    val logoImageUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val tags: YearTagCounter? = null
)

@Serializable
data class EventDao(
    val slug: String,
    val groupSlug: String,
    val name: String,
    val description: String? = null,
    val tags: String? = null,
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
    val venueName: String? = null,
    val venueLon: Double? = null,
    val venueLat: Double? = null,
    val venueCountry: String? = null,
    val venueCity: String? = null,
    val venueAddress: String? = null,
    val meetupLink: String? = null
) {
    fun getTagList() = tags?.takeIf { it.isNotEmpty() }
        ?.split(" ")
        ?.map { knownTagsByHashtag.getValue(it) }
}

@Serializable
data class SpeakerDao(
    val name: String,
    val speakerDeckId: String? = null,
    val slideShareId: String? = null,
    val tags: YearTagCounter? = null
) {
    val slug: String = name.toSlug()
}

@Serializable
data class SlideDao(
    val slug: String,
    val name: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val speakerSlug: String,
    val author: String,
    val image: String? = null,
    val tags: String? = null
) {
    fun getTagList() = tags?.takeIf { it.isNotEmpty() }
        ?.split(" ")
        ?.map { knownTagsByHashtag.getValue(it) }
}

@Serializable
class TagDao(
    val name: String,
    val hashtag: String,
    val slug: String,
    val group: Map<String, YearCounter>? = null,
    val event: Map<String, List<String>>? = null,
    val speaker: Map<String, YearCounter>? = null,
    val slide: Map<String, List<String>>? = null
)