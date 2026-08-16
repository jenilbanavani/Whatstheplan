package com.example.whatstheplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.whatstheplan.ui.WhatsThePlanApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    private val destinationFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destinationFlow.value = intent.getStringExtra(EXTRA_DESTINATION)
        val app = application as WhatsThePlanApplication

        setContent {
            WhatsThePlanApp(
                container = app.container,
                notificationDestinationFlow = destinationFlow.asStateFlow(),
                consumeNotificationDestination = { destinationFlow.value = null },
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destinationFlow.value = intent.getStringExtra(EXTRA_DESTINATION)
    }

    companion object {
        const val EXTRA_DESTINATION = "destination"
        const val DESTINATION_CHECK_IN = "check_in"
        const val DESTINATION_MORNING = "morning"
        const val DESTINATION_REFLECTION = "reflection"
    }
}
