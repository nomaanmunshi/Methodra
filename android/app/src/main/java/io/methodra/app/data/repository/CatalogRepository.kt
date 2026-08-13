package io.methodra.app.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.methodra.app.domain.*
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val methods: List<MethodDefinition> by lazy { loadMethods() }
    val rules: List<MatchingRule> by lazy { loadRules() }
    val collections: List<BookProtocolCollection> by lazy { loadCollections() }

    fun method(id: String): MethodDefinition? = methods.firstOrNull { it.id == id }

    private fun readAsset(name: String): String = context.assets.open(name).bufferedReader().use { it.readText() }

    private fun loadMethods(): List<MethodDefinition> {
        val array = JSONArray(readAsset("methods.json"))
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            val evidenceObj = o.getJSONObject("evidence")
            MethodDefinition(
                id = o.getString("id"),
                name = o.getString("name"),
                shortExplanation = o.getString("shortExplanation"),
                intendedProblem = o.getString("intendedProblem"),
                mayHelpWhen = o.stringList("mayHelpWhen"),
                unsuitableWhen = o.stringList("unsuitableWhen"),
                evidence = EvidenceAssessment(
                    level = EvidenceLevel.valueOf(evidenceObj.getString("level")),
                    limitation = evidenceObj.getString("limitation"),
                    rationale = evidenceObj.getString("rationale")
                ),
                inspiration = o.getString("inspiration"),
                setupQuestions = o.stringList("setupQuestions"),
                steps = o.getJSONArray("steps").let { steps ->
                    (0 until steps.length()).map { stepIndex ->
                        steps.getJSONObject(stepIndex).let { s ->
                            ProtocolStep(s.getInt("order"), s.getString("title"), s.getString("instruction"))
                        }
                    }
                },
                minimumVersion = o.getString("minimumVersion"),
                focusRule = if (o.isNull("focusRule")) null else o.getString("focusRule"),
                outcomeMetric = o.getString("outcomeMetric"),
                reviewDays = o.getInt("reviewDays"),
                stopConditions = o.stringList("stopConditions"),
                sourceLabels = o.stringList("sourceLabels"),
                sourceUrls = o.stringList("sourceUrls"),
                goalDomains = o.stringList("goalDomains").map(GoalDomain::valueOf).toSet(),
                obstacles = o.stringList("obstacles").map(ObstacleType::valueOf).toSet()
            )
        }.also { methods ->
            require(methods.size == 10) { "Version 1 must expose exactly ten research-supported methods" }
            require(methods.map { it.id }.distinct().size == methods.size) { "Duplicate method id" }
            methods.forEach { method ->
                require(method.evidence.limitation.isNotBlank()) { "Evidence limitation is required for ${method.id}" }
                require(method.sourceLabels.isNotEmpty()) { "At least one source is required for ${method.id}" }
                require(method.sourceLabels.size == method.sourceUrls.size) { "Source label/URL mismatch for ${method.id}" }
                require(method.sourceUrls.all { it.isBlank() || it.startsWith("https://") || it.startsWith("http://") }) { "Invalid source URL for ${method.id}" }
                require(method.steps.isNotEmpty()) { "Protocol steps are required for ${method.id}" }
            }
        }
    }

    private fun loadCollections(): List<BookProtocolCollection> {
        val array = JSONArray(readAsset("book-collections.json"))
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            BookProtocolCollection(
                id = o.getString("id"),
                title = o.getString("title"),
                book = o.getString("book"),
                author = o.getString("author"),
                evidenceLevel = EvidenceLevel.valueOf(o.getString("evidenceLevel")),
                summary = o.getString("summary"),
                steps = o.stringList("steps"),
                evidenceNote = o.getString("evidenceNote")
            )
        }.also { require(it.size == 4) { "Version 1 must expose exactly four book-inspired collections" } }
    }

    private fun loadRules(): List<MatchingRule> {
        val array = JSONArray(readAsset("method-rules.json"))
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            MatchingRule(
                id = o.getString("id"),
                methodId = o.getString("methodId"),
                field = o.getString("field"),
                operator = o.getString("operator"),
                values = o.stringList("values"),
                score = o.getInt("score"),
                reason = o.getString("reason")
            )
        }.also { validateRules(it) }
    }

    private fun validateRules(rules: List<MatchingRule>) {
        require(rules.map { it.id }.distinct().size == rules.size) { "Duplicate matching rule id" }
        val methodIds = methods.map { it.id }.toSet()
        rules.forEach {
            require(it.methodId in methodIds) { "Unknown method in rule ${it.id}" }
            require(it.score in 1..100) { "Rule score outside 1..100: ${it.id}" }
            require(it.reason.isNotBlank()) { "Rule reason must be visible to users" }
        }
    }

    private fun org.json.JSONObject.stringList(key: String): List<String> {
        val array = getJSONArray(key)
        return (0 until array.length()).map(array::getString)
    }
}
