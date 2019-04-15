package com.github.omarmiatello.gdgtools

import com.github.omarmiatello.gdgtools.data.FireDB
import com.github.omarmiatello.gdgtools.data.SpeakerDao
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.initialize() {
    route("initialize") {
        route("db") {
            get("speaker") {
                val speakers = listOf(
                    SpeakerDao("Omar Miatello", speakerDeckId = "jacklt"),
                    SpeakerDao("Valentina Carrirolo", speakerDeckId = "carrirolo"),
                    SpeakerDao("Marco Gomiero", speakerDeckId = "prof18"),
                    SpeakerDao("Laura Morinigo", speakerDeckId = "lauramorinigo"),
                    SpeakerDao("Giorgio Antonioli", speakerDeckId = "fondesa"),
                    SpeakerDao("Fabio Collini", slideShareId = "fabio_collini"),
                    SpeakerDao("Michelantonio Trizio", speakerDeckId = "mikelantonio"),
                    SpeakerDao("Roberto Orgiu", speakerDeckId = "tiwiz"),
                    SpeakerDao("Boris D'Amato", slideShareId = "damatoboris"),
                    SpeakerDao("Leonardo Pirro", speakerDeckId = "lpirro"),
                    SpeakerDao("Giuseppe Filograno", slideShareId = "GiuseppeFilograno"),
                    SpeakerDao("Lorenzo Quiroli", speakerDeckId = "quiro91")
                )
                FireDB.addSpeakers(speakers)
                call.respondText(speakers.joinToString("\n"))
            }
        }
    }
}
