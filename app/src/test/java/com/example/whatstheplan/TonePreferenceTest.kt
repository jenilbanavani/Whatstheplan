package com.example.whatstheplan

import com.example.whatstheplan.domain.model.TonePreference
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TonePreferenceTest {

    @Test
    fun `test morning greetings contain user name when present`() {
        val calmGreeting = TonePreference.CALM.morningGreeting("Alex")
        assertTrue(calmGreeting.contains("Alex"))

        val directGreeting = TonePreference.DIRECT.morningGreeting("Alex")
        assertTrue(directGreeting.contains("Alex"))

        val playfulGreeting = TonePreference.PLAYFUL.morningGreeting(null)
        assertFalse(playfulGreeting.contains("null"))
    }

    @Test
    fun `test follow up title and body in each tone`() {
        val calmTitle = TonePreference.CALM.followUpTitle()
        assertTrue(calmTitle.contains("Gentle"))

        val directTitle = TonePreference.DIRECT.followUpTitle()
        assertTrue(directTitle.contains("Priority"))

        val playfulTitle = TonePreference.PLAYFUL.followUpTitle()
        assertTrue(playfulTitle.contains("vibe check"))

        val calmBody = TonePreference.CALM.followUpBody("Finish report", "Open document")
        assertTrue(calmBody.contains("Open document"))
    }

    @Test
    fun `test status feedback handles DONE, MOVED, DROPPED without guilt`() {
        val calmDone = TonePreference.CALM.statusFeedback("DONE")
        assertTrue(calmDone.isNotBlank())

        val calmMoved = TonePreference.CALM.statusFeedback("MOVED")
        assertTrue(calmMoved.contains("fresh start") || calmMoved.contains("okay") || calmMoved.contains("shift"))

        val calmDropped = TonePreference.CALM.statusFeedback("DROPPED")
        assertTrue(calmDropped.contains("okay") || calmDropped.contains("conscious"))
    }
}
