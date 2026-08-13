package io.methodra.backend.methods;

import io.methodra.backend.methods.MethodModels.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeterministicMethodEngine {
    private final CatalogService catalog;

    public DeterministicMethodEngine(CatalogService catalog) {
        this.catalog = catalog;
    }

    public List<MethodMatch> match(MatchingInput input) {
        Map<String, Integer> scores = new HashMap<>();
        Map<String, List<String>> reasons = new HashMap<>();

        for (MatchingRule rule : catalog.rules()) {
            if (matches(rule, input)) {
                scores.merge(rule.methodId(), rule.score(), Integer::sum);
                reasons.computeIfAbsent(rule.methodId(), ignored -> new ArrayList<>()).add(rule.reason());
            }
        }

        for (MethodDefinition method : catalog.methods()) {
            if (method.goalDomains().contains(input.goalDomain())) scores.merge(method.id(), 6, Integer::sum);
            if (method.obstacles().contains(input.obstacle())) scores.merge(method.id(), 8, Integer::sum);
        }

        return catalog.methods().stream()
                .map(method -> new MethodMatch(method, scores.getOrDefault(method.id(), 0), distinct(reasons.getOrDefault(method.id(), List.of()))))
                .filter(match -> match.score() > 0)
                .sorted(Comparator.comparingInt(MethodMatch::score).reversed().thenComparing(match -> match.method().id()))
                .limit(3)
                .map(match -> match.reasons().isEmpty()
                        ? new MethodMatch(match.method(), match.score(), List.of("This method matches the goal domain and obstacle you selected."))
                        : match)
                .toList();
    }

    private boolean matches(MatchingRule rule, MatchingInput input) {
        String actual = switch (rule.field()) {
            case "goalDomain" -> input.goalDomain().name();
            case "obstacle" -> input.obstacle().name();
            case "highScreenTime" -> Boolean.toString(input.highScreenTime());
            case "structureLevel" -> input.structureLevel().name();
            case "availableMinutes" -> Integer.toString(input.availableMinutes());
            default -> null;
        };
        if (actual == null) return false;
        return switch (rule.operator()) {
            case "eq" -> !rule.values().isEmpty() && rule.values().getFirst().equalsIgnoreCase(actual);
            case "in" -> rule.values().stream().anyMatch(v -> v.equalsIgnoreCase(actual));
            case "lte" -> !rule.values().isEmpty() && Integer.parseInt(actual) <= Integer.parseInt(rule.values().getFirst());
            case "gte" -> !rule.values().isEmpty() && Integer.parseInt(actual) >= Integer.parseInt(rule.values().getFirst());
            default -> false;
        };
    }

    private static List<String> distinct(List<String> values) { return new ArrayList<>(new LinkedHashSet<>(values)); }
}
