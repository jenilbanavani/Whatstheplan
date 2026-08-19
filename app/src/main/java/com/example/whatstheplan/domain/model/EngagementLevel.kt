package com.example.whatstheplan.domain.model

enum class EngagementLevel(
    val levelCode: Int,
    val title: String,
    val description: String,
) {
    LEVEL_0_NORMAL(
        levelCode = 0,
        title = "Normal (Attentive)",
        description = "Active companion loop. Morning prompt, midday ping, and evening reflection scheduled.",
    ),
    LEVEL_1_DISTRACTED(
        levelCode = 1,
        title = "Distracted / Busy",
        description = "One dismissal, high latency, or DND detected. Subsequent pings delayed by 3.5 hours.",
    ),
    LEVEL_2_STRESSED(
        levelCode = 2,
        title = "Stressed (Dormant Mode)",
        description = "Fast swipe, consecutive dismissals, or terse reply. Muted for the rest of today.",
    ),
    LEVEL_3_GHOSTING(
        levelCode = 3,
        title = "Passive Observer (Ghosting)",
        description = "48+ hours without interaction. Push notifications halted until manual app launch.",
    ),
    ;

    val isDormant: Boolean get() = this == LEVEL_2_STRESSED
    val isPassiveObserver: Boolean get() = this == LEVEL_3_GHOSTING
}
