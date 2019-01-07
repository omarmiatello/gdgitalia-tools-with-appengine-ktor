package com.github.gdgitalia.tools

import com.github.gdgitalia.tools.appengine.cacheUriOr
import com.github.gdgitalia.tools.data.*
import com.github.gdgitalia.tools.utils.toJsonPretty
import io.ktor.application.call
import io.ktor.html.Template
import io.ktor.html.insert
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.list


@Serializable
data class TagJsonResponse(val name: String, val slug: String, val hashtag: String)

fun Routing.gdg() {
    route("gdg") {
        get("groups.json") {
            call.respondText(cacheUriOr {
                val groups = FireDB.allGroups.toGroupResponseList()
                groups.toJsonPretty(GroupResponse.serializer().list)
            })
        }

        get("groups_events.json") {
            call.respondText(cacheUriOr {
                val eventsMap = FireDB.eventsMap
                val groupsAndEvents = FireDB.allGroups.map {
                    val events = eventsMap[it.slug]?.values?.toEventResponseList()
                    it.toResponse(events)
                }
                groupsAndEvents.toJsonPretty(GroupResponse.serializer().list)
            })
        }

        get("groups") {
            val eventsMap = FireDB.eventsMap
            val groupsAndEvents = FireDB.allGroups.map {
                val events = eventsMap[it.slug]?.values?.toEventResponseList()
                it.toResponse(events)
            }
            call.respondHtml {
                head { title { +"GDG Italia - Tools Project" } }
                body {
                    h1 { +"GDG Italia" }
                    p { a("groups.json") { +"groups.json" } }
                    p { a("groups_events.json") { +"groups_events.json" } }
                    p { +"Ci sono ${groupsAndEvents.count()} GDG in Italia" }

                    h1 { +"Gruppi" }
                    groupsAndEvents.forEach { group ->
                        val events = group.events

                        div {
                            insert(group.toHtml(showGroupDescription = false)) {}
                            br
                            a("/gdg/groups/${group.slug}") { +"Ulteriori dettagli" }
                        }
                        h3 { +"Eventi (${events?.count() ?: 0})" }
                        p {
                            events?.forEach { event ->
                                insert(event.toHtml(showEventDescription = false)) {}
                                br
                            }
                        }
                    }
                }
            }
        }

        get("groups/{gdgId}.json") {
            cacheUriOr {
                val gdgId = call.parameters["gdgId"]!!
                val events = FireDB.getEventsBy(gdgId).values.toEventResponseList()
                val group = FireDB.getGroup(gdgId)?.toResponse(events)
                group?.toJsonPretty(GroupResponse.serializer()).orEmpty()
            }.also {
                if (it.isEmpty()) call.respond(HttpStatusCode.NotFound) else call.respondText(it)
            }
        }

        get("groups/{gdgId}") {
            val gdgId = call.parameters["gdgId"]!!
            val events = FireDB.getEventsBy(gdgId).values.toEventResponseList()
            val group = FireDB.getGroup(gdgId)!!.toResponse(events)
            call.respondHtml {
                head { title { +"GDG Italia - Tools Project" } }
                body {
                    a("$gdgId.json") { +"$gdgId.json" }
                    div {
                        insert(group.toHtml(showGroupDescription = true)) {}
                    }
                    h3 { +"Eventi (${events.count()})" }
                    p {
                        events.forEach { event ->
                            insert(event.toHtml(showEventDescription = true)) {}
                            br
                        }
                    }
                }
            }
        }

        get("speakers.json") {
            call.respondText(cacheUriOr {
                val speakers = FireDB.speakersMap.values.toSpeakerResponseList()
                speakers.toJsonPretty(SpeakerResponse.serializer().list)
            })
        }

        get("speakers_slides.json") {
            call.respondText(cacheUriOr {
                val slidesMap = FireDB.slidesMap
                val speakersAndSlides = FireDB.speakersMap.values.map {
                    val slides = slidesMap[it.slug]?.values?.toSlideResponseList()
                    it.toResponse(slides)
                }
                speakersAndSlides.toJsonPretty(SpeakerResponse.serializer().list)
            })
        }

        get("speakers/{speaker}.json") {
            cacheUriOr {
                val speakerParam = call.parameters["speaker"]!!
                val slides = FireDB.getSlidesBy(speakerParam).values.toSlideResponseList()
                val speaker = FireDB.getSpeaker(speakerParam)?.toResponse(slides)
                speaker?.toJsonPretty(SpeakerResponse.serializer()).orEmpty()
            }.also {
                if (it.isEmpty()) call.respond(HttpStatusCode.NotFound) else call.respondText(it)
            }
        }

        get("tag.json") {
            call.respondText(cacheUriOr {
                val tagMap = FireDB.tagsMap.values.toTagResponseList()
                tagMap.toJsonPretty(TagFullResponse.serializer().list)
            })
        }

        get("tag") {
            call.respondHtml {
                head { title { +"GDG Italia - Tools Project" } }
                body {
                    h1 { +"GDG Italia" }
                    knownTags.forEach {
                        p {
                            a("tag/${it.slug}") { +it.hashtag }
                            +" | "
                            a("tag/${it.slug}.json") { +"${it.slug}.json" }
                        }
                    }
                }
            }
        }

        get("tag/{tag}.json") {
            cacheUriOr {
                val tagParam = call.parameters["tag"]!!
                val tag = FireDB.getTag(tagParam)?.toFullResponse()
                tag?.toJsonPretty(TagFullResponse.serializer()).orEmpty()
            }.also {
                if (it.isEmpty()) call.respond(HttpStatusCode.NotFound) else call.respondText(it)
            }
        }

        get("tag/{tag}") {
            val tagParam = call.parameters["tag"]!!
            val tag = FireDB.getTag(tagParam)?.toFullResponse()
            if (tag != null) {
                val tagEvents = tag.event.orEmpty()
                call.respondHtml {
                    head { title { +"GDG Italia - Tools Project" } }
                    body {
                        h1 { +tag.hashtag }
                        a("$tagParam.json") { +"$tagParam.json" }
                        tagEvents.forEach { (groupSlug, eventSlugs) ->
                            h2 { +"$groupSlug (${eventSlugs.count()} eventi)" }
                            eventSlugs.forEach { eventSlug ->
                                p {
                                    a("/gdg/groups/$groupSlug") { +groupSlug }
                                    +eventSlug
                                }
                            }
                        }
                    }
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private fun GroupResponse.toHtml(showGroupDescription: Boolean) = object : Template<FlowContent> {
    override fun FlowContent.apply() {
        h2 {
            +("$name (")
            a(meetupLink) { +"Link gruppo" }
            +(")")
        }
        img(src = logoImageUrl)
        p {
            +"Persone registrate: $membersCount $membersName"
            br
            +"Luogo: $city"
            if (showGroupDescription && description != null) {
                br
                +"Descrizione: "
                unsafe { raw(description) }
            }
        }
    }
}

private fun EventResponse.toHtml(showEventDescription: Boolean) = object : Template<FlowContent> {
    override fun FlowContent.apply() {
        div {
            b { +name }
            if (meetupLink != null) {
                +" ("
                a(meetupLink) { +"Link evento" }
                +")"
            }
            if (!tags.isNullOrEmpty()) {
                br
                tags.forEach { tag ->
                    a("/gdg/tag/${tag.slug}") { +tag.hashtag }
                    +" "
                }
            }
            if (time != 0L) {
                br
                +"$dateString dalle $timeString - Durata: ${duration / 3600 / 1000f} ore"
            }
            if (venueName != null) {
                br
                +"Location: "
                +"$venueName ($venueCity, $venueAddress - $venueCountry)"
            }
            br
            +"Iscritti: $attendeeRsvpYesCount"
            if (attendeeRsvpLimit != 0) +"/$attendeeRsvpLimit"
            if (attendeeWaitlistCount != 0) +" (waitlist: $attendeeWaitlistCount)"
            if (attendeeManualCount != 0) {
                +" | Hanno partecipato: "
                b { +"$attendeeManualCount" }
            }
            if (showEventDescription && description != null) {
                br
                +"Descrizione: "
                unsafe { raw(description) }
            }
        }
    }
}