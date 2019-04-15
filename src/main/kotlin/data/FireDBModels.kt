package com.github.omarmiatello.gdgtools.data

import com.github.omarmiatello.gdgtools.utils.toSlug
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

typealias YearCounter = Map<String, Int>

typealias YearTagCounter = Map<String, Map<String, Int>>

@Serializable
data class GroupDao(
    val slug: String,
    val name: String,
    val membersCount: Int,
    val membersName: String,
    @Optional val description: String? = null,
    @Optional val lat: Double? = null,
    @Optional val lon: Double? = null,
    @Optional val region: String? = null,
    @Optional val created: Long? = null,
    @Optional val city: String? = null,
    @Optional val country: String? = null,
    @Optional val meetupLink: String? = null,
    @Optional val logoImageUrl: String? = null,
    @Optional val backgroundImageUrl: String? = null,
    @Optional val tags: YearTagCounter? = null
)

@Serializable
data class EventDao(
    val slug: String,
    val groupSlug: String,
    val name: String,
    @Optional val description: String? = null,
    @Optional val tags: String? = null,
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
    @Optional val venueName: String? = null,
    @Optional val venueLon: Double? = null,
    @Optional val venueLat: Double? = null,
    @Optional val venueCountry: String? = null,
    @Optional val venueCity: String? = null,
    @Optional val venueAddress: String? = null,
    @Optional val meetupLink: String? = null
) {
    fun getTagList() = tags?.takeIf { it.isNotEmpty() }
        ?.split(" ")
        ?.map { knownTagsByHashtag[it]!! }
}

@Serializable
data class SpeakerDao(
    val name: String,
    @Optional val speakerDeckId: String? = null,
    @Optional val slideShareId: String? = null,
    val slug: String = name.toSlug(),
    @Optional val tags: YearTagCounter? = null
)

@Serializable
data class SlideDao(
    val slug: String,
    val name: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val speakerSlug: String,
    val author: String,
    @Optional val image: String? = null,
    @Optional val tags: String? = null
) {
    fun getTagList() = tags?.takeIf { it.isNotEmpty() }
        ?.split(" ")
        ?.map { knownTagsByHashtag[it]!! }
}

@Serializable
class TagDao(
    val name: String,
    val hashtag: String,
    val slug: String,
    @Optional val group: Map<String, YearCounter>? = null,
    @Optional val event: Map<String, List<String>>? = null,
    @Optional val speaker: Map<String, YearCounter>? = null,
    @Optional val slide: Map<String, List<String>>? = null
)