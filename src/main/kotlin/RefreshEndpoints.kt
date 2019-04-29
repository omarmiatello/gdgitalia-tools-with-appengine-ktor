package com.github.omarmiatello.gdgtools

import com.github.omarmiatello.gdgtools.appengine.MeetupApi
import com.github.omarmiatello.gdgtools.appengine.SlideShareApi
import com.github.omarmiatello.gdgtools.appengine.SpeakerDeckApi
import com.github.omarmiatello.gdgtools.data.FireDB
import com.github.omarmiatello.gdgtools.data.GroupDao
import com.github.omarmiatello.gdgtools.data.YearTagCounter
import com.github.omarmiatello.gdgtools.data.knownTags
import com.github.omarmiatello.gdgtools.utils.toJsonPretty
import com.github.omarmiatello.gdgtools.utils.toSlug
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.serialization.list

fun Routing.refresh() {
    route("refresh") {
        get("firebase/from/meetup") {
            val meetupGroups = MeetupApi.findGroups("gdg").filter { it.country == "IT" } + MeetupApi.findGroups("Google Cloud Developer Community").filter { it.country == "IT" }
            FireDB.allGroups = meetupGroups.map { it.toDao() }
            FireDB.meetupUrlnamesMap = meetupGroups.associateBy { it.gdgName.toSlug() }.mapValues { it.value.urlname }
            call.respondText(meetupGroups.map { it.toDao() }.toJsonPretty(GroupDao.serializer().list))
        }

        get("firebase/from/meetup/events") {
            call.respondText(buildString {
                val urlnamesMap = FireDB.meetupUrlnamesMap
                appendln("${urlnamesMap.count()} GDG found in Firebase")
                urlnamesMap.forEach { (groupSlug, urlnames) ->
                    val events = MeetupApi.getEvents(urlnames).map { it.toDao() }
                    FireDB.addEvents(events)
                    appendln("Saved ${events.count()} events\tfrom $groupSlug")
                }
                append("ok")
            })
        }

        get("firebase/from/db/slides") {
            call.respondText(buildString {
                FireDB.speakersMap.values.forEach { speaker ->
                    val speakerSlug = speaker.slug

                    val speakerDeckSlides = if (speaker.speakerDeckId != null) {
                        SpeakerDeckApi.getSlides(speaker.speakerDeckId).map { it.toSlideDao(speakerSlug) }
                    } else emptyList()

                    val slideShareSlides = if (speaker.slideShareId != null) {
                        SlideShareApi.getSlides(speaker.slideShareId).map { it.toSlideDao(speakerSlug) }
                    } else emptyList()

                    val slides = speakerDeckSlides + slideShareSlides
                    FireDB.addSlides(slides)
                    appendln("${speaker.name} (${slides.count()} slides)\n${slides.joinToString("\n")}\n")
                }
            })
        }

        get("firebase/from/db/tags") {
            call.respondText(buildString {
                val eventsByGroupSlug = FireDB.eventsMap.mapValues { it.value.values }
                val groupsTags: Map<String, YearTagCounter> = eventsByGroupSlug.mapValues { (_, allEvents) ->
                    val slidesByYear = allEvents.groupBy { it.slug.take(4) }
                    slidesByYear.mapValues { (_, events) ->
                        knownTags.map { tag ->
                            tag.slug to events.count { tag.hashtag in it.tags.orEmpty() }
                        }
                            .filterNot { it.second == 0 }
                            .toMap()
                    }
                }
                groupsTags.forEach { groupSlug, tags -> FireDB.addGroupTags(groupSlug, tags) }

                val slidesBySpeakerSlug = FireDB.slidesMap.mapValues { it.value.values }
                val speakersTags: Map<String, YearTagCounter> = slidesBySpeakerSlug.mapValues { (_, allSlides) ->
                    val slidesByYear = allSlides.groupBy { it.pubDate.take(4) }
                    slidesByYear.mapValues { (_, slides) ->
                        knownTags.map { tag ->
                            tag.slug to slides.count { tag.hashtag in it.tags.orEmpty() }
                        }
                            .filterNot { it.second == 0 }
                            .toMap()
                    }
                }
                speakersTags.forEach { speakerSlug, tags -> FireDB.addSpeakerTags(speakerSlug, tags) }

                val hashtagToSlug = knownTags.map { it.hashtag to it.slug }.toMap()

                val tagToGroupToYearCounter = mutableMapOf<String, MutableMap<String, MutableMap<String, Int>>>()
                val tagToEvent = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
                eventsByGroupSlug.values.flatten().forEach { event ->
                    val hashtags = event.tags.orEmpty().split(" ").filter { it.isNotEmpty() }
                    hashtags.forEach { hashtag ->
                        val tagSlug = hashtagToSlug[hashtag]!!
                        val year = event.slug.take(4)
                        val groupSlug = event.groupSlug
                        val counter = tagToGroupToYearCounter
                            .getOrPut(tagSlug) { mutableMapOf() }
                            .getOrPut(groupSlug) { mutableMapOf() }
                        counter[year] = counter.getOrElse(year) { 0 } + 1

                        val eventList = tagToEvent
                            .getOrPut(tagSlug) { mutableMapOf() }
                            .getOrPut(groupSlug) { mutableListOf() }
                        eventList += event.slug
                    }
                }

                val tagToSpeakerToYearCounter = mutableMapOf<String, MutableMap<String, MutableMap<String, Int>>>()
                val tagToSlide = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
                slidesBySpeakerSlug.values.flatten().forEach { event ->
                    val hashtags = event.tags.orEmpty().split(" ").filter { it.isNotEmpty() }
                    hashtags.forEach { hashtag ->
                        val tagSlug = hashtagToSlug[hashtag]!!
                        val year = event.pubDate.take(4)
                        val speakerSlug = event.speakerSlug
                        val counter = tagToSpeakerToYearCounter
                            .getOrPut(tagSlug) { mutableMapOf() }
                            .getOrPut(speakerSlug) { mutableMapOf() }
                        counter[year] = counter.getOrElse(year) { 0 } + 1

                        val eventList = tagToSlide
                            .getOrPut(tagSlug) { mutableMapOf() }
                            .getOrPut(speakerSlug) { mutableListOf() }
                        eventList += event.slug
                    }
                }

                FireDB.tagsMap = knownTags.map {
                    val tagSlug = it.slug
                    it.toFullDao(
                        tagToGroupToYearCounter[tagSlug].orEmpty(),
                        tagToEvent[tagSlug].orEmpty(),
                        tagToSpeakerToYearCounter[tagSlug].orEmpty(),
                        tagToSlide[tagSlug].orEmpty()
                    )
                }.associateBy { it.slug }

                appendln("OK")
            })
        }
    }
}
