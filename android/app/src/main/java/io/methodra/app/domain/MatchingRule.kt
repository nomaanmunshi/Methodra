package io.methodra.app.domain

data class MatchingRule(
    val id: String,
    val methodId: String,
    val field: String,
    val operator: String,
    val values: List<String>,
    val score: Int,
    val reason: String
)
