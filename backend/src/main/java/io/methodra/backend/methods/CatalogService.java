package io.methodra.backend.methods;

import io.methodra.backend.methods.MethodModels.*;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CatalogService {
    private final List<MethodDefinition> methods;
    private final List<MatchingRule> rules;
    private final List<BookProtocolCollection> collections;

    public CatalogService() {
        this.methods = List.copyOf(loadMethods());
        this.rules = List.copyOf(loadRules());
        this.collections = List.copyOf(loadCollections());
        validate();
    }

    public List<MethodDefinition> methods() { return methods; }
    public List<MatchingRule> rules() { return rules; }
    public List<BookProtocolCollection> collections() { return collections; }
    public Optional<MethodDefinition> method(String id) { return methods.stream().filter(m -> m.id().equals(id)).findFirst(); }

    @SuppressWarnings("unchecked")
    private List<MethodDefinition> loadMethods() {
        JsonParser parser = JsonParserFactory.getJsonParser();
        List<Object> root = parser.parseList(read("methods.json"));
        List<MethodDefinition> out = new ArrayList<>();
        for (Object item : root) {
            Map<String, Object> m = (Map<String, Object>) item;
            Map<String, Object> e = (Map<String, Object>) m.get("evidence");
            List<ProtocolStep> steps = ((List<Object>) m.get("steps")).stream().map(raw -> {
                Map<String, Object> s = (Map<String, Object>) raw;
                return new ProtocolStep(intValue(s.get("order")), str(s.get("title")), str(s.get("instruction")));
            }).toList();
            out.add(new MethodDefinition(
                    str(m.get("id")), str(m.get("name")), str(m.get("shortExplanation")), str(m.get("intendedProblem")),
                    strings(m.get("mayHelpWhen")), strings(m.get("unsuitableWhen")),
                    new EvidenceAssessment(EvidenceLevel.valueOf(str(e.get("level"))), str(e.get("limitation")), str(e.get("rationale"))),
                    str(m.get("inspiration")), strings(m.get("setupQuestions")), steps,
                    str(m.get("minimumVersion")), nullableString(m.get("focusRule")), str(m.get("outcomeMetric")), intValue(m.get("reviewDays")),
                    strings(m.get("stopConditions")), strings(m.get("sourceLabels")), strings(m.get("sourceUrls")),
                    enumSet(strings(m.get("goalDomains")), GoalDomain.class), enumSet(strings(m.get("obstacles")), ObstacleType.class)
            ));
        }
        return out;
    }


    @SuppressWarnings("unchecked")
    private List<BookProtocolCollection> loadCollections() {
        JsonParser parser = JsonParserFactory.getJsonParser();
        List<Object> root = parser.parseList(read("book-collections.json"));
        return root.stream().map(raw -> {
            Map<String, Object> m = (Map<String, Object>) raw;
            return new BookProtocolCollection(
                    str(m.get("id")), str(m.get("title")), str(m.get("book")), str(m.get("author")),
                    EvidenceLevel.valueOf(str(m.get("evidenceLevel"))), str(m.get("summary")),
                    strings(m.get("steps")), str(m.get("evidenceNote"))
            );
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<MatchingRule> loadRules() {
        JsonParser parser = JsonParserFactory.getJsonParser();
        List<Object> root = parser.parseList(read("method-rules.json"));
        return root.stream().map(raw -> {
            Map<String, Object> m = (Map<String, Object>) raw;
            return new MatchingRule(str(m.get("id")), str(m.get("methodId")), str(m.get("field")), str(m.get("operator")), strings(m.get("values")), intValue(m.get("score")), str(m.get("reason")));
        }).toList();
    }

    private void validate() {
        if (methods.size() != 10) throw new IllegalStateException("Version 1 catalog must contain exactly 10 research-supported methods");
        Set<String> methodIds = new HashSet<>();
        for (MethodDefinition method : methods) {
            if (!methodIds.add(method.id())) throw new IllegalStateException("Duplicate method id: " + method.id());
            if (method.evidence().limitation().isBlank()) throw new IllegalStateException("Evidence limitation missing: " + method.id());
            if (method.sourceLabels().isEmpty()) throw new IllegalStateException("Sources missing: " + method.id());
            if (method.sourceLabels().size() != method.sourceUrls().size()) throw new IllegalStateException("Source label/URL mismatch: " + method.id());
            if (method.sourceUrls().stream().anyMatch(url -> !url.isBlank() && !(url.startsWith("https://") || url.startsWith("http://"))))
                throw new IllegalStateException("Invalid source URL: " + method.id());
        }
        if (collections.size() != 4) throw new IllegalStateException("Version 1 catalog must contain exactly 4 book-inspired collections");
        Set<String> collectionIds = new HashSet<>();
        for (BookProtocolCollection collection : collections) {
            if (!collectionIds.add(collection.id())) throw new IllegalStateException("Duplicate collection id: " + collection.id());
            if (collection.evidenceNote().isBlank()) throw new IllegalStateException("Collection evidence note missing: " + collection.id());
        }

        Set<String> ruleIds = new HashSet<>();
        for (MatchingRule rule : rules) {
            if (!ruleIds.add(rule.id())) throw new IllegalStateException("Duplicate rule id: " + rule.id());
            if (!methodIds.contains(rule.methodId())) throw new IllegalStateException("Unknown method in rule: " + rule.id());
            if (rule.score() < 1 || rule.score() > 100) throw new IllegalStateException("Rule score out of range: " + rule.id());
            if (rule.reason().isBlank()) throw new IllegalStateException("Rule reason missing: " + rule.id());
        }
    }

    private String read(String name) {
        try {
            return new ClassPathResource(name).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load " + name, e);
        }
    }

    private static String str(Object value) { return Objects.toString(value, ""); }
    private static String nullableString(Object value) { return value == null ? null : value.toString(); }
    private static int intValue(Object value) { return value instanceof Number n ? n.intValue() : Integer.parseInt(value.toString()); }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        if (value == null) return List.of();
        return ((List<Object>) value).stream().map(Object::toString).toList();
    }

    private static <E extends Enum<E>> Set<E> enumSet(List<String> values, Class<E> type) {
        Set<E> result = new LinkedHashSet<>();
        values.forEach(value -> result.add(Enum.valueOf(type, value)));
        return Set.copyOf(result);
    }
}
