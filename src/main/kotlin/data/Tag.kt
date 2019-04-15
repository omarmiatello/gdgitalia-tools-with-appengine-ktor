package com.github.omarmiatello.gdgtools.data

import com.github.omarmiatello.gdgtools.config.AppConfig
import com.github.omarmiatello.gdgtools.utils.toSlug

operator fun String.contains(tag: Tag): Boolean {
    return (listOf(tag.name) + tag.alias).firstOrNull { it in this } != null
}

fun String.findTags() = knownTags.filter { it in this }

fun String.findHashtags() = findTags().map { it.hashtag }

private val config = AppConfig.getDefault().telegram

class Tag(
    val name: String,
    val alias: List<String> = emptyList(),
    val hashtag: String = "#${name.filter { it.isLetterOrDigit() }}",
    val slug: String = name.toSlug()
) {
    val channelId = config.tag_chatId_map[slug].takeUnless { it.isNullOrEmpty() }
    val channelName = config.chatId_name_map[channelId].takeUnless { it.isNullOrEmpty() }
    val telegramLink = channelId?.takeIf { it.startsWith("@") }?.let { "https://t.me/${it.drop(1)}" }

    fun toFullDao(
        group: Map<String, YearCounter>,
        event: Map<String, List<String>>,
        speaker: Map<String, YearCounter>,
        slide: Map<String, List<String>>
    ) = TagDao(name, hashtag, slug, group, event, speaker, slide)

    fun toBasicResponse() = TagBasicResponse(name, hashtag, slug)

    fun toCounterResponse(count: Int) = TagCounterResponse(name, hashtag, slug, count)
}


val knownTags = listOf(
    // type
    Tag(name = "Google I/O", alias = listOf("Google io", "I/O")),
    Tag(name = "DevFest", alias = listOf("dev fest")),
    Tag(name = "AperiTech"),
    Tag(name = "Hash Code", alias = listOf("hashcode")),
    Tag(name = "Team Building", alias = listOf("Birrata", "Picnic", "birretta", "Brainstorm")),
    Tag(name = "Codelab", alias = listOf("Workshop")),
    Tag(name = "Viewing Party"),
    Tag(name = "Study Jam", alias = listOf("StudyJam")),
    Tag(name = "Women Techmakers", alias = listOf("WTM")),
    Tag(name = "Community", alias = listOf("Social")),

    // technology
    Tag(name = "Android"),
    Tag(name = "Assistant", alias = listOf("Actions", "Action on")),
    Tag(name = "Kotlin"),
    Tag(name = "Firebase"),
    Tag(name = "Flutter"),
    Tag(name = "Web", alias = listOf("Webapp")),
    Tag(name = "Progressive Web App", alias = listOf("PWA")),
    Tag(name = "Google Cloud", alias = listOf("GCP", "Google Cloud Platform", "Cloud Functions")),
    Tag(name = "App Engine", alias = listOf("GAE", "AppEngine")),
    Tag(name = "Cloud Next", alias = listOf("Next")),
    Tag(name = "Container", alias = listOf("Docker", "Kubernetes")),
    Tag(name = "Chrome"),
    Tag(name = "Machine Learning", alias = listOf("ML", "TensorFlow", "Tensor Flow", "Intelligence")),
    Tag(name = "Augmented Reality", alias = listOf("ARCore", "AR Core", "AR Kit")),
    Tag(name = "IOT", alias = listOf("Android Things", "Internet of Things")),
    Tag(name = "JavaScript", alias = listOf("JS"))
)

val knownTagsBySlug = knownTags.associateBy { it.slug }

val knownTagsByHashtag = knownTags.associateBy { it.hashtag }