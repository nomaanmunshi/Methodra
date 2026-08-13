package io.methodra.backend.methods;

import java.util.List;
import java.util.Set;

public final class MethodModels {
    private MethodModels() {}

    public enum GoalDomain { STUDY, WORK, HEALTHY_ROUTINE, DIGITAL_WELLBEING, PERSONAL_PROJECT, GENERAL }
    public enum ObstacleType { DELAYED_START, DIGITAL_DISTRACTION, FORGETTING, INCONSISTENCY, OVERWHELM, PASSIVE_STUDY, TASK_SWITCHING, LOW_AUTONOMY, TEMPTATION, RECOVERY }
    public enum StructureLevel { LIGHT, MODERATE, HIGH }
    public enum EvidenceLevel { A, B, C, D }

    public record EvidenceAssessment(EvidenceLevel level, String limitation, String rationale) {}
    public record ProtocolStep(int order, String title, String instruction) {}
    public record MethodDefinition(
            String id,
            String name,
            String shortExplanation,
            String intendedProblem,
            List<String> mayHelpWhen,
            List<String> unsuitableWhen,
            EvidenceAssessment evidence,
            String inspiration,
            List<String> setupQuestions,
            List<ProtocolStep> steps,
            String minimumVersion,
            String focusRule,
            String outcomeMetric,
            int reviewDays,
            List<String> stopConditions,
            List<String> sourceLabels,
            List<String> sourceUrls,
            Set<GoalDomain> goalDomains,
            Set<ObstacleType> obstacles
    ) {}

    public record MatchingRule(
            String id,
            String methodId,
            String field,
            String operator,
            List<String> values,
            int score,
            String reason
    ) {}

    public record MatchingInput(
            GoalDomain goalDomain,
            String desiredOutcome,
            ObstacleType obstacle,
            int availableMinutes,
            boolean highScreenTime,
            StructureLevel structureLevel,
            String pastFailure
    ) {}

    public record MethodMatch(MethodDefinition method, int score, List<String> reasons) {}

    public record BookProtocolCollection(
            String id,
            String title,
            String book,
            String author,
            EvidenceLevel evidenceLevel,
            String summary,
            List<String> steps,
            String evidenceNote
    ) {}
}
