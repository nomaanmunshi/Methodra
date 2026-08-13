package io.methodra.app.data.repository

import io.methodra.app.data.local.PersonalTrialEntity
import io.methodra.app.data.local.TrialDao
import io.methodra.app.data.local.TrialEntryEntity
import io.methodra.app.domain.TrialSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrialRepository @Inject constructor(
    private val dao: TrialDao
) {
    val activeTrial: Flow<PersonalTrialEntity?> = dao.observeActiveTrial()

    fun entries(trialId: String): Flow<List<TrialEntryEntity>> = dao.observeEntries(trialId)

    suspend fun create(question: String, methodId: String, primaryMetric: String, plannedDays: Int = 14) {
        dao.upsertTrial(
            PersonalTrialEntity(
                question = question.ifBlank { "Does this method help me follow the protocol more consistently?" },
                methodId = methodId,
                primaryMetricName = primaryMetric.ifBlank { "Useful output" },
                startedLocalDate = LocalDate.now().toString(),
                plannedDays = plannedDays.coerceIn(7, 84)
            )
        )
    }

    suspend fun log(trialId: String, adhered: Boolean, metricValue: Double?, note: String) {
        dao.upsertEntry(
            TrialEntryEntity(
                trialId = trialId,
                localDate = LocalDate.now().toString(),
                adhered = adhered,
                metricValue = metricValue,
                contextNote = note
            )
        )
    }

    suspend fun decide(trialId: String, decision: String) {
        val normalized = decision.uppercase()
        dao.decide(trialId, normalized, if (normalized == "CONTINUE" || normalized == "MODIFY" || normalized == "SIMPLIFY") "ACTIVE" else "CLOSED")
    }

    fun summarize(entries: List<TrialEntryEntity>): TrialSummary {
        val adherenceRate = if (entries.isEmpty()) 0 else (entries.count { it.adhered } * 100 / entries.size)
        val values = entries.mapNotNull { it.metricValue }
        val average = values.takeIf { it.isNotEmpty() }?.average()
        val language = when {
            entries.size < 3 -> "Insufficient data. Keep logging before interpreting the trial."
            else -> "During this trial, adherence was $adherenceRate%. Treat this as an association in your own tracking, not proof that the method caused the outcome."
        }
        return TrialSummary(entries.size, adherenceRate, average, language)
    }
}
