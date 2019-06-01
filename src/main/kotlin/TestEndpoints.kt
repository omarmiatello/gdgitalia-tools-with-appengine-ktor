package com.github.omarmiatello.gdgtools

import com.github.omarmiatello.gdgtools.appengine.MeetupApi
import com.github.omarmiatello.gdgtools.utils.DeferredLazy
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.request.host
import io.ktor.request.uri
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.coroutineScope
import kotlinx.html.*

fun Routing.test() {
    suspend fun PipelineContext<Unit, ApplicationCall>.resp(msg: String) {
        call.respondHtml {
            head { title { +"GDG Italia - Tools Project - Test" } }
            body {
                h3 {
                    a("/") { +call.request.host() }
                    +call.request.uri
                }
                p { pre { +msg } }
            }
        }
    }

    route("test") {
        val gdgGroups = DeferredLazy { MeetupApi.findGroups("gdg").map { it.toDao() } }
        get("cached") {
            resp(coroutineScope { gdgGroups.await() }.toString())
        }
    }
}

