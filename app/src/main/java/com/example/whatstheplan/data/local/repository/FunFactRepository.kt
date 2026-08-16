package com.example.whatstheplan.data.local.repository

import com.example.whatstheplan.data.local.FunFactDataSource
import com.example.whatstheplan.domain.model.FunFact
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class FunFactRepository(
    private val settingsRepository: SettingsRepository,
) {
    val facts: List<FunFact> = FunFactDataSource.facts

    suspend fun nextFactOrNull(): FunFact? {
        if (!settingsRepository.settingsFlow.first().funFactsEnabled) return null
        return nextFact()
    }

    suspend fun nextFact(): FunFact {
        val recent = settingsRepository.recentFactIds().toSet()
        val candidates = facts.filterNot { it.id in recent }.ifEmpty { facts }
        val fact = candidates.random(Random(System.currentTimeMillis()))
        settingsRepository.markFactShown(fact.id)
        return fact
    }
}
