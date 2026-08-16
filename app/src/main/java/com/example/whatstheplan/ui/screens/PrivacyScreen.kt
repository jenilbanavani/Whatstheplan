package com.example.whatstheplan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whatstheplan.ui.components.CardTitle
import com.example.whatstheplan.ui.components.SectionCard

@Composable
fun PrivacyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Privacy", style = MaterialTheme.typography.displaySmall)
        SectionCard {
            CardTitle("Offline by design", Icons.Default.Lock)
            PrivacyLine("No account required.")
            PrivacyLine("No personal data is uploaded.")
            PrivacyLine("No cloud database is used.")
            PrivacyLine("Plans, check-ins, and reflections remain on this device.")
            PrivacyLine("Screen-time information remains on this device.")
            PrivacyLine("The app does not sell personal data.")
        }
        SectionCard {
            CardTitle("Publishing note")
            Text(
                text = "Google Play data safety declarations still need to be configured separately when publishing.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrivacyLine(text: String) {
    Text("- $text", style = MaterialTheme.typography.bodyLarge)
}
