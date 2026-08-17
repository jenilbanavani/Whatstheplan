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
        val namePrefix = if (!userName.isNullOrBlank()) ", $userName" else ""
        return when (this) {
            CALM -> "Good morning$namePrefix ☀️ Take your time to set a clear intention for today."
            PLAYFUL -> "Rise and shine$namePrefix! ✨ What exciting or important thing are we tackling today?"
            DIRECT -> "Morning$namePrefix. What is your one priority today?"
        }
    }

    fun followUpTitle(): String = when (this) {
        CALM -> "Gentle check-in 👋"
        PLAYFUL -> "Quick vibe check! ✨"
        DIRECT -> "Priority check 🎯"
    }

    fun followUpBody(intention: String?, firstStep: String?): String {
        val hasStep = !firstStep.isNullOrBlank()
        val target = if (hasStep) "first step: \"$firstStep\"" else (intention?.let { "\"$it\"" } ?: "your daily intention")
        return when (this) {
            CALM -> "How is your plan going? If you have 10 minutes, try your $target."
            PLAYFUL -> "Still feeling good about $target? Let's give it a quick 10-minute spin!"
            DIRECT -> "Status update: Ready to start $target?"
        }
    }

    fun eveningPrompt(): String = when (this) {
        CALM -> "How did today unfold? Let's peacefully close out the day."
        PLAYFUL -> "Day complete! 🎉 How did things turn out with your plan?"
        DIRECT -> "End of day review. Did you complete your intention?"
    }

    fun statusFeedback(status: String): String = when (status) {
        "DONE" -> when (this) {
            CALM -> "Nicely done. You followed through on what you intended."
            PLAYFUL -> "Boom! Mission accomplished 🎉"
            DIRECT -> "Completed. Good execution."
        }
        "MOVED" -> when (this) {
            CALM -> "No problem at all. Priorities shift and tomorrow is a fresh start."
            PLAYFUL -> "Shifted to later! Life happens, we'll get it next time."
            DIRECT -> "Rescheduled for next time."
        }
        "DROPPED" -> when (this) {
            CALM -> "A conscious decision to let it go today. That is completely okay."
            PLAYFUL -> "Dropped for today! Rest is just as important."
            DIRECT -> "Acknowledged and dropped for today."
        }
        else -> "Intention updated."
    }
}
