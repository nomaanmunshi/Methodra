package io.methodra.backend.methods;

import io.methodra.backend.methods.MethodModels.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicMethodEngineTest {
    private final CatalogService catalog = new CatalogService();
    private final DeterministicMethodEngine engine = new DeterministicMethodEngine(catalog);

    @Test
    void catalogHasExactlyTenV1MethodsWithLimitations() {
        assertEquals(10, catalog.methods().size());
        assertTrue(catalog.methods().stream().allMatch(m -> !m.evidence().limitation().isBlank()));
        assertEquals(4, catalog.collections().size());
    }

    @Test
    void delayedStudyStartRanksIfThenAndReturnsAtMostThree() {
        var input = new MatchingInput(
                GoalDomain.STUDY,
                "Attempt one operating-systems retrieval question after breakfast",
                ObstacleType.DELAYED_START,
                35,
                true,
                StructureLevel.MODERATE,
                "I open social media first"
        );
        List<MethodMatch> result = engine.match(input);
        assertTrue(result.size() <= 3);
        assertEquals("if_then_start", result.getFirst().method().id());
        assertFalse(result.getFirst().reasons().isEmpty());
    }

    @Test
    void sameInputAlwaysReturnsSameOrdering() {
        var input = new MatchingInput(GoalDomain.STUDY, "Learn", ObstacleType.DIGITAL_DISTRACTION, 45, true, StructureLevel.HIGH, "");
        assertEquals(engine.match(input).stream().map(m -> m.method().id()).toList(),
                engine.match(input).stream().map(m -> m.method().id()).toList());
    }
}
