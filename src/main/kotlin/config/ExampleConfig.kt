package com.github.omarmiatello.gdgtools.config

object ExampleConfig : AppConfig() {
    init {
        meetup {
            apiKey = ""
        }
        telegram {
            apiKey = ""

            // chatId: Unique identifier for the target chat or username of the target channel (in the format @channelusername)

            chatId_all_events = ""

            tag_chatId_map = mapOf(
                "google-io" to "@community_gdgitalia",
                "devfest" to "",
                "aperitech" to "",
                "hash-code" to "",
                "team-building" to "",
                "codelab" to "",
                "viewing-party" to "",
                "study-jam" to "",
                "women-techmakers" to "",
                "community" to "",
                "android" to "",
                "assistant" to "",
                "kotlin" to "",
                "firebase" to "",
                "flutter" to "",
                "web" to "",
                "progressive-web-app" to "",
                "google-cloud" to "",
                "app-engine" to "",
                "cloud-next" to "",
                "container" to "",
                "chrome" to "",
                "machine-learning" to "",
                "augmented-reality" to "",
                "iot" to "",
                "javascript" to ""
            )

            chatId_name_map = mapOf(
                "@community_gdgitalia" to "Community",
                "@mobile_gdgitalia" to "Mobile app",
                "@ai_gdgitalia" to "AI + Assistant",
                "@web_gdgitalia" to "Cloud + Web"
            )
        }
        rss2json {
            apiKey = ""
        }
    }
}