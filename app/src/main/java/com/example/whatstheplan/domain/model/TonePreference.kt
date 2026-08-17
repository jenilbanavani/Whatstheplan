package com.example.whatstheplan.domain.model

enum class TonePreference(
    val title: String,
    val description: String,
) {
    CALM(
        title = "Calm",
        description = "Gentle, supportive, and unhurried",
    ),
    PLAYFUL(
        title = "Playful",
        description = "Light-hearted, friendly, and curious",
    ),
    DIRECT(
        title = "Direct",
        description = "Clear, concise, and straight to the point",
    ),
    ;

    fun morningGreeting(userName: String?): String {
        val nameSuffix = if (!userName.isNullOrBlank()) ", $userName" else ""
        return when (this) {
            CALM -> "Good morning$nameSuffix. What is your one priority today?"
            PLAYFUL -> "Good morning$nameSuffix 👀 What's the one thing today?"
            DIRECT -> "Morning$nameSuffix. Your one thing today:"
        }
    }

    fun followUpTitle(): String = when (this) {
        CALM -> "Gentle check-in"
        PLAYFUL -> "Project time 👀"
        DIRECT -> "Priority check"
    }

    fun followUpBody(intention: String?, firstStep: String?): String {
        val target = if (!firstStep.isNullOrBlank()) firstStep else (intention ?: "your intention")
        return when (this) {
            CALM -> "Still realistic? If you have 10 minutes, try: $target"
            PLAYFUL -> "You planned: \"$target\". Still realistic?"
            DIRECT -> "Status: Still realistic to start \"$target\"?"
        }
    }

    fun eveningPrompt(intention: String? = null): String {
        val name = if (!intention.isNullOrBlank()) "\"$intention\"" else "Today's plan"
        return when (this) {
            CALM -> "$name — How did today unfold? Move it, shrink it, or drop it?"
            PLAYFUL -> "$name didn't happen today? Move it, shrink it, or drop it?"
            DIRECT -> "End of day: Did $name get done, moved, or dropped?"
        }
    }

    fun statusFeedback(status: String): String = when (status) {
        "DONE" -> when (this) {
            CALM -> "Nicely done. You followed through on what you intended."
            PLAYFUL -> "Done! Mission accomplished 🎉"
            DIRECT -> "Completed. Good execution."
        }
        "MOVED" -> when (this) {
            CALM -> "Priorities shift. Tomorrow is a fresh start."
            PLAYFUL -> "Moved to later! Life happens, we'll get it next time."
            DIRECT -> "Rescheduled for tomorrow."
        }
        "DROPPED" -> when (this) {
            CALM -> "A conscious decision to let it go today. That is completely okay."
            PLAYFUL -> "Dropped for today! Rest is just as important."
            DIRECT -> "Acknowledged and dropped for today."
        }
        else -> "Intention updated."
    }
}
