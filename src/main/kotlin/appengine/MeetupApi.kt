package com.github.omarmiatello.gdgtools.appengine

import com.github.omarmiatello.gdgtools.config.AppConfig
import com.github.omarmiatello.gdgtools.data.EventDao
import com.github.omarmiatello.gdgtools.data.GroupDao
import com.github.omarmiatello.gdgtools.data.findHashtags
import com.github.omarmiatello.gdgtools.utils.cleanGdgName
import com.github.omarmiatello.gdgtools.utils.formatFull
import com.github.omarmiatello.gdgtools.utils.parse
import com.github.omarmiatello.gdgtools.utils.toSlug
import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.UrlEncodedContent
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.list
import java.util.*

// https://www.meetup.com/it-IT/meetup_api/


object MeetupApi {
    private val config = AppConfig.getDefault().meetup
    private val basePath = "https://api.meetup.com"

    private val accessToken by lazy {
        // val logger = Logger.getLogger(MeetupApi::class.java.name)
        // fun log(msg: String) = logger.log(Level.INFO, msg)
        // Authentication.authorize().also { log("accessToken ${it.parseAsString()}")  }
        // Authentication.access().also { log("accessToken ${it.parseAsString()}") }

        Authentication.refreshToken()
            .parse(Authentication.OAuthResponse.serializer())!!
            .access_token
    }

    private val httpTransport = UrlFetchTransport.getDefaultInstance()
    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory()

    private fun get(methodPath: String) = requestFactory()
        .buildGetRequest(GenericUrl("$basePath/$methodPath"))
        .setHeaders(HttpHeaders().setAuthorization("Bearer $accessToken"))
        .execute()

    fun findGroups(
        query: String,
        radius: String = "global",
        limit: Int = 50,
        page: Int = 0,
        lat: Double = 41.9,
        lon: Double = 12.48,
        orderType: String = "distance",
        country: String = "IT"
    ) =
        get("find/groups?country=$country&text=${query.encodeURLQueryComponent()}&radius=$radius&page=$limit&offset=$page&lat=$lat&lon=$lon&order=$orderType")
            .parse(MeetupGroup.serializer().list)
            .orEmpty()

    fun getEvents(
        groupUrlName: String,
        status: String = "past,upcoming"
    ) =
        get("$groupUrlName/events?status=$status")
            .parse(MeetupEvent.serializer().list)
            .orEmpty()


    // Build with help of https://app.quicktype.io

    @Serializable
    data class MeetupGroup(
        val name: String,
        val link: String,
        val urlname: String,
        val description: String,
        val created: Long,
        val city: String,
        val country: String,
        val lat: Double,
        val lon: Double,
        val members: Int,
        val organizer: Organizer,
        val who: String,
        val key_photo: Photo? = null,
        val group_photo: Photo? = null
    ) {
        @Transient
        val gdgName = name.cleanGdgName()

        fun toDao(): GroupDao {
            return GroupDao(
                slug = gdgName.toSlug(),
                name = gdgName,
                membersCount = members,
                membersName = who,
                description = description,
                lat = lat,
                lon = lon,
                region = null,
                created = created,
                city = city,
                country = country,
                meetupLink = link,
                logoImageUrl = group_photo?.photo_link?.ifBlank { null }
                    ?: organizer.photo?.photo_link?.ifBlank { null },
                backgroundImageUrl = key_photo?.photo_link?.ifBlank { null }
            )
        }
    }

    @Serializable
    data class MeetupEvent(
        val created: Long,
        val duration: Long = 0,
        val name: String,
        val rsvp_limit: Int = 0,
        val time: Long,
        val local_date: String,
        val local_time: String,
        val rsvp_close_offset: String? = null,
        val updated: Long,
        val waitlist_count: Int,
        val yes_rsvp_count: Int,
        val venue: Venue? = null,
        val group: EventGroup,
        val link: String,
        val manual_attendance_count: Int = 0,
        val description: String? = null,
        val rsvp_open_offset: String? = null
    ) {
        fun toDao() = EventDao(
            slug = "$local_date $name".toSlug(),
            groupSlug = group.name.cleanGdgName().toSlug(),
            name = name,
            description = description,
            tags = (name + description).findHashtags().joinToString(" "),
            dateString = Date(time).formatFull(),
            timeString = local_time,
            created = created,
            duration = duration,
            time = time,
            updated = updated,
            attendeeRsvpYesCount = yes_rsvp_count,
            attendeeRsvpLimit = rsvp_limit,
            attendeeWaitlistCount = waitlist_count,
            attendeeManualCount = manual_attendance_count,
            venueName = venue?.name?.ifBlank { null },
            venueLon = venue?.lon,
            venueLat = venue?.lat,
            venueCountry = venue?.country?.ifBlank { null },
            venueCity = venue?.city?.ifBlank { null },
            venueAddress = venue?.address_1?.ifBlank { null },
            meetupLink = link.ifBlank { null }
        )
    }

    @Serializable
    data class EventGroup(
        val name: String
    )

    @Serializable
    data class Venue(
        val name: String,
        val lat: Double? = null,
        val lon: Double? = null,
        val address_1: String? = null,
        val city: String? = null,
        val country: String? = null,
        val localized_country_name: String? = null
    )

    @Serializable
    data class Photo(
        val highres_link: String? = null,
        val photo_link: String,
        val thumb_link: String,
        val type: String,
        val base_url: String
    )

    @Serializable
    data class Organizer(
        val name: String,
        val bio: String,
        val photo: Photo? = null
    )

    // Authentication OAuth2 - https://www.meetup.com/it-IT/meetup_api/auth/#oauth2
    object Authentication {
        @Serializable
        data class OAuthResponse(val access_token: String)

        fun authorize() = requestFactory()
                .buildGetRequest(GenericUrl(
                    "https://secure.meetup.com/oauth2/authorize?client_id=${config.oauthClientId}" +
                    "&response_type=code&redirect_uri=${config.oauthRedirect}"
                ))
                .execute()

        fun access(clientCode: String)=  requestFactory()
                .buildPostRequest(GenericUrl(
                    "https://secure.meetup.com/oauth2/access"
                ), UrlEncodedContent(mapOf("client_id" to config.oauthClientId,
                        "client_secret" to config.oauthClientSecret ,
                        "grant_type" to "authorization_code",
                        "redirect_uri" to config.oauthRedirect,
                        "code" to clientCode)
                ))
                .execute()

        fun refreshToken(refreshToken: String = config.oauthClientRefreshToken) = requestFactory()
                .buildPostRequest(GenericUrl(
                    "https://secure.meetup.com/oauth2/access"
                ), UrlEncodedContent(mapOf("client_id" to config.oauthClientId,
                        "client_secret" to config.oauthClientSecret ,
                        "grant_type" to "refresh_token",
                        "refresh_token" to refreshToken)
                ))
                .execute()
    }
}