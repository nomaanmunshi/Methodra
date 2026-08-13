package io.methodra.app.data.repository

import io.methodra.app.data.local.ActiveProtocolEntity
import io.methodra.app.data.local.DailyProtocolStateEntity
import io.methodra.app.data.local.ProtocolDao
import io.methodra.app.domain.ProtocolProgressLogic
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtocolRepository @Inject constructor(
    private val dao: ProtocolDao
) {
    val activeProtocol: Flow<ActiveProtocolEntity?> = dao.observeActive()

    suspend fun activate(
        methodId: String,
        desiredOutcome: String,
        reasons: List<String> = emptyList(),
        setupSummary: String = ""
    ): ActiveProtocolEntity {
        dao.deactivateAll()
        val entity = ActiveProtocolEntity(
            methodId = methodId,
            desiredOutcome = desiredOutcome.ifBlank { "Build a repeatable protocol" },
            matchReasonsText = reasons.distinct().take(3).joinToString("\n"),
            setupSummaryText = setupSummary.trim(),
            startedAtEpochMillis = System.currentTimeMillis()
        )
        dao.insert(entity)
        return entity
    }

    fun dailyState(protocolId: String, date: LocalDate = LocalDate.now()): Flow<DailyProtocolStateEntity?> =
        dao.observeDaily(protocolId, date.toString())

    suspend fun toggleStep(protocolId: String, stepIndex: Int, date: LocalDate = LocalDate.now()) {
        val existing = dao.getDaily(protocolId, date.toString())
        val completedCsv = ProtocolProgressLogic.toggleCompleted(existing?.completedStepIndexesCsv, stepIndex)
        dao.upsertDaily(
            (existing ?: DailyProtocolStateEntity(protocolId = protocolId, localDate = date.toString()))
                .copy(
                    completedStepIndexesCsv = completedCsv,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
        )
    }

    suspend fun checkIn(
        protocolId: String,
        rating: Int,
        note: String,
        automaticityRating: Int? = null,
        recoveryReason: String = "",
        date: LocalDate = LocalDate.now()
    ) {
        val existing = dao.getDaily(protocolId, date.toString())
        dao.upsertDaily(
            (existing ?: DailyProtocolStateEntity(protocolId = protocolId, localDate = date.toString()))
                .copy(
                    checkInRating = ProtocolProgressLogic.normalizedRating(rating),
                    automaticityRating = ProtocolProgressLogic.normalizedAutomaticity(automaticityRating),
                    recoveryReason = if (ProtocolProgressLogic.recoveryIsUseful(rating)) recoveryReason else "",
                    note = note,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
        )
    }
}
