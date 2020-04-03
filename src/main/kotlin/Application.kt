package com.github.omarmiatello.gdgtools

import com.github.omarmiatello.gdgtools.appengine.AppEngineCache
import com.github.omarmiatello.gdgtools.data.FireDB
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.*

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * For more information about this file: https://ktor.io/servers/configuration.html#hocon-file
 */
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(StatusPages)

    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html
        get("/") {
            val cache = AppEngineCache(useLocalCache = true)
            val groups = cache.getOrPut("apiGroups") { FireDB.allGroups.map { it.slug }.joinToString("|") }
                .split("|")
            val speakers = cache.getOrPut("apiSpeaker") { FireDB.speakersMap.map { it.value.slug }.joinToString("|") }
                .split("|")
            val tags = cache.getOrPut("apiTag") { FireDB.tagsMap.map { it.value.slug }.joinToString("|") }
                .split("|")

            call.respondHtml {

                head {
                    meta { charset = "utf-8" }
                    meta("viewport", "width=device-width, initial-scale=1")
                    title { +"GDG Italia - Tools Project" }
                    styleLink("https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.2/css/bulma.min.css")
                }
                body {
                    section("section") {
                        div("container") {
                            h1("title") {
                                +"Website ("
                                a("https://github.com/omarmiatello/gdgitalia-tools-with-appengine-ktor") {
                                    +"source"
                                }
                                +")"
                            }
                            p { a("/gdg/groups") { +"groups" } }
                            p { a("/gdg/groups/gdg-milano") { +"groups/gdg-milano" } }
                            p { a("/gdg/tag") { +"tag" } }
                            p { a("/gdg/tag/devfest") { +"tag/devfest" } }
                            p { a("/gdg/calendar/2020") { +"calendar/2020" } }
                            h1("title") { +"API" }
                            div("columns") {
                                div("column") {
                                    h2("subtitle") { +"Groups API" }
                                    p { a("/gdg/groups.json") { +"groups.json" } }
                                    p { a("/gdg/groups_events.json") { +"groups_events.json" } }
                                    groups.forEach { slug ->
                                        p { a("/gdg/groups/$slug.json") { +"groups/$slug.json" } }
                                    }
                                }
                                div("column") {
                                    h2("subtitle") { +"Speakers API" }
                                    p { a("/gdg/speakers.json") { +"speakers.json" } }
                                    p { a("/gdg/speakers_slides.json") { +"speakers_slides.json" } }
                                    speakers.forEach { slug ->
                                        p { a("/gdg/speakers/$slug.json") { +"speakers/$slug.json" } }
                                    }
                                }
                                div("column") {
                                    h2("subtitle") { +"Tags API" }
                                    p { a("/gdg/tag.json") { +"tag.json" } }
                                    tags.forEach { slug ->
                                        p { a("/gdg/tag/$slug.json") { +"tag/$slug.json" } }
                                    }
                                }
                            }
                            h1("title") { +"Telegram channel" }
                            p { a("https://t.me/gdgeventi") { +"@gdgeventi" } }
                        }
                    }
                }
            }
        }

        gdg()
        notification()
        initialize()
        refresh()
        test()
    }
}
