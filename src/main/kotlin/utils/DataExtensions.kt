package com.github.omarmiatello.gdgtools.utils

import java.text.Normalizer

// Slug Utils

private val NONLATIN = "[^\\w-]".toRegex()

private val WHITESPACE = "[\\s]".toRegex()

fun String.toSlug(): String {
    val nowhitespace = WHITESPACE.replace(this, "-")
    val normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
    return NONLATIN.replace(normalized, "").toLowerCase()
}

fun String.cleanGdgName() = replaceAfter(" - ", "")
    .replace("\u0027", "'")
    .replace("Google Developer Group", "GDG", ignoreCase = true)
    .replace("GDG-", "GDG ", ignoreCase = true)
    .replace("Google Cloud Developer Community", "GCDC", ignoreCase = true)
    .replace("GCDC-", "GCDC", ignoreCase = true)
    .replace("Meetup", "", ignoreCase = true)
    .replace(" - ", "")
    .trim()
