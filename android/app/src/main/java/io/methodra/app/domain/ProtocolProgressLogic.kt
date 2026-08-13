package io.methodra.app.domain

/** Pure progression rules kept outside Room so recovery behavior is deterministic and cheap to test. */
object ProtocolProgressLogic {
    fun toggleCompleted(csv: String?, stepIndex: Int): String {
        require(stepIndex >= 0) { "stepIndex must be non-negative" }
        val completed = csv.orEmpty()
            .split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it >= 0 }
            .toMutableSet()
        if (!completed.add(stepIndex)) completed.remove(stepIndex)
        return completed.sorted().joinToString(",")
    }

    fun normalizedRating(rating: Int): Int = rating.coerceIn(1, 5)

    fun normalizedAutomaticity(rating: Int?): Int? = rating?.coerceIn(1, 5)

    fun recoveryIsUseful(rating: Int): Boolean = normalizedRating(rating) <= 2
}
