package com.example.whatstheplan.domain.model

enum class ActivityType(
    val emoji: String,
    val label: String,
) {
    WORKING("🎯", "Working"),
    STUDYING("📚", "Studying"),
    BUILDING("💻", "Building something"),
    GAMING("🎮", "Gaming"),
    SCROLLING("📱", "Scrolling"),
    WATCHING("🎬", "Watching something"),
    BREAKING("😴", "Taking a break"),
    OUTSIDE("🚶", "Outside"),
    OTHER("✍️", "Other"),
    ;

    val displayName: String = "$emoji $label"

    companion object {
        fun fromCode(code: String): ActivityType =
            entries.firstOrNull { it.name == code } ?: OTHER
    }
}
