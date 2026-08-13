package io.methodra.app.domain

enum class GoalDomain(val label: String) {
    STUDY("Study"), WORK("Work"), HEALTHY_ROUTINE("Healthy routine"), DIGITAL_WELLBEING("Digital wellbeing"), PERSONAL_PROJECT("Personal project"), GENERAL("General")
}

enum class ObstacleType(val label: String) {
    DELAYED_START("I delay starting"), DIGITAL_DISTRACTION("I open distracting apps"), FORGETTING("I forget"), INCONSISTENCY("I am inconsistent"), OVERWHELM("The plan is too large"), PASSIVE_STUDY("I study passively"), TASK_SWITCHING("I keep switching tasks"), LOW_AUTONOMY("The goal does not feel like mine"), TEMPTATION("A more enjoyable option wins"), RECOVERY("I struggle after missing a day")
}

enum class StructureLevel(val label: String) { LIGHT("Light"), MODERATE("Moderate"), HIGH("High") }

enum class EvidenceLevel(val label: String) {
    A("Strong support"), B("Promising support"), C("Practical framework"), D("Reflection")
}

data class EvidenceAssessment(
    val level: EvidenceLevel,
    val limitation: String,
    val rationale: String
)

data class ProtocolStep(
    val order: Int,
    val title: String,
    val instruction: String
)

data class MethodDefinition(
    val id: String,
    val name: String,
    val shortExplanation: String,
    val intendedProblem: String,
    val mayHelpWhen: List<String>,
    val unsuitableWhen: List<String>,
    val evidence: EvidenceAssessment,
    val inspiration: String,
    val setupQuestions: List<String>,
    val steps: List<ProtocolStep>,
    val minimumVersion: String,
    val focusRule: String?,
    val outcomeMetric: String,
    val reviewDays: Int,
    val stopConditions: List<String>,
    val sourceLabels: List<String>,
    val sourceUrls: List<String>,
    val goalDomains: Set<GoalDomain>,
    val obstacles: Set<ObstacleType>
)

data class MatchingInput(
    val goalDomain: GoalDomain,
    val desiredOutcome: String,
    val obstacle: ObstacleType,
    val availableMinutes: Int,
    val highScreenTime: Boolean,
    val structureLevel: StructureLevel,
    val pastFailure: String = ""
)

data class MethodMatch(
    val method: MethodDefinition,
    val score: Int,
    val reasons: List<String>
)

data class AssessmentDraft(
    val goalDomain: GoalDomain = GoalDomain.STUDY,
    val desiredOutcome: String = "",
    val obstacle: ObstacleType = ObstacleType.DELAYED_START,
    val availableMinutes: Int = 35,
    val highScreenTime: Boolean = true,
    val structureLevel: StructureLevel = StructureLevel.MODERATE,
    val pastFailure: String = ""
) {
    fun toInput() = MatchingInput(goalDomain, desiredOutcome, obstacle, availableMinutes, highScreenTime, structureLevel, pastFailure)
}

data class UsageApp(val packageName: String, val label: String, val minutesToday: Long = 0)

data class TrialSummary(
    val daysLogged: Int,
    val adherenceRate: Int,
    val averageMetric: Double?,
    val language: String
)


data class BookProtocolCollection(
    val id: String,
    val title: String,
    val book: String,
    val author: String,
    val evidenceLevel: EvidenceLevel,
    val summary: String,
    val steps: List<String>,
    val evidenceNote: String
)
