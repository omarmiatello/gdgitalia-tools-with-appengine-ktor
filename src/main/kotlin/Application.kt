package com.github.gdgitalia.tools

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
            call.respondHtml {
                head { title { +"GDG Italia - Tools Project" } }
                body {
                    h4 { +"Website" }
                    p { a("/gdg/groups") { +"gdg/groups" } }
                    p { a("/gdg/groups/gdg-milano") { +"gdg/groups/gdg-milano" } }
                    p { a("/gdg/tag") { +"gdg/tag" } }
                    p { a("/gdg/tag/devfest") { +"gdg/tag/devfest" } }
                    h4 { +"API" }
                    p { a("/gdg/groups.json") { +"gdg/groups.json" } }
                    p { a("/gdg/groups_events.json") { +"gdg/groups_events.json" } }
                    p { a("/gdg/groups/gdg-milano.json") { +"gdg/groups/gdg-milano.json" } }
                    p { a("/gdg/speakers.json") { +"gdg/speakers.json" } }
                    p { a("/gdg/speakers_slides.json") { +"gdg/speakers_slides.json" } }
                    p { a("/gdg/speakers/omar-miatello.json") { +"gdg/speakers/omar-miatello.json" } }
                    p { a("/gdg/tag.json") { +"gdg/tag.json" } }
                    p { a("/gdg/tag/devfest.json") { +"gdg/tag/devfest.json" } }
                    h4 { +"Telegram channel" }
                    p { a("https://t.me/gdgeventi") { +"@gdgeventi" } }
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
