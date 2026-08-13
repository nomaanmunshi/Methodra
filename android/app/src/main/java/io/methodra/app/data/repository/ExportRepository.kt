package io.methodra.app.data.repository

import io.methodra.app.data.local.FocusDao
import io.methodra.app.data.local.ProtocolDao
import io.methodra.app.data.local.TrialDao
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    private val protocolDao: ProtocolDao,
    private val focusDao: FocusDao,
    private val trialDao: TrialDao
) {
    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("format", "methodra-export-v1")
        root.put("exportedAtEpochMillis", System.currentTimeMillis())
        root.put("protocols", JSONArray(protocolDao.getAllProtocols().map {
            JSONObject().put("id", it.id).put("methodId", it.methodId).put("desiredOutcome", it.desiredOutcome)
                .put("matchReasonsText", it.matchReasonsText).put("setupSummaryText", it.setupSummaryText)
                .put("startedAtEpochMillis", it.startedAtEpochMillis).put("active", it.active)
        }))
        root.put("dailyProtocolState", JSONArray(protocolDao.getAllDailyStates().map {
            JSONObject().put("protocolId", it.protocolId).put("localDate", it.localDate)
                .put("completedStepIndexesCsv", it.completedStepIndexesCsv).put("checkInRating", it.checkInRating)
                .put("automaticityRating", it.automaticityRating).put("recoveryReason", it.recoveryReason)
                .put("note", it.note)
        }))
        root.put("focusRules", JSONArray(focusDao.getRules().map {
            JSONObject().put("packageName", it.packageName).put("appLabel", it.appLabel)
                .put("blockDuringFocus", it.blockDuringFocus).put("dailyBudgetMinutes", it.dailyBudgetMinutes)
        }))
        root.put("focusSchedules", JSONArray(focusDao.getSchedules().map {
            JSONObject().put("id", it.id).put("name", it.name).put("daysMask", it.daysMask)
                .put("startMinuteOfDay", it.startMinuteOfDay).put("endMinuteOfDay", it.endMinuteOfDay).put("enabled", it.enabled)
        }))
        root.put("focusSessions", JSONArray(focusDao.getAllSessions().map {
            JSONObject().put("id", it.id).put("startedAtEpochMillis", it.startedAtEpochMillis)
                .put("plannedEndEpochMillis", it.plannedEndEpochMillis).put("endedAtEpochMillis", it.endedAtEpochMillis)
                .put("emergencyExit", it.emergencyExit).put("exitReason", it.exitReason)
        }))
        root.put("trials", JSONArray(trialDao.getAllTrials().map {
            JSONObject().put("id", it.id).put("question", it.question).put("methodId", it.methodId)
                .put("primaryMetricName", it.primaryMetricName).put("startedLocalDate", it.startedLocalDate)
                .put("plannedDays", it.plannedDays).put("status", it.status).put("decision", it.decision)
        }))
        root.put("trialEntries", JSONArray(trialDao.getAllEntries().map {
            JSONObject().put("trialId", it.trialId).put("localDate", it.localDate).put("adhered", it.adhered)
                .put("metricValue", it.metricValue).put("contextNote", it.contextNote)
        }))
        return root.toString(2)
    }
}
