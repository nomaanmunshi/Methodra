package io.methodra.backend.api;

import io.methodra.backend.methods.CatalogService;
import io.methodra.backend.methods.DeterministicMethodEngine;
import io.methodra.backend.methods.MethodModels.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MethodController {
    private final CatalogService catalog;
    private final DeterministicMethodEngine engine;

    public MethodController(CatalogService catalog, DeterministicMethodEngine engine) {
        this.catalog = catalog;
        this.engine = engine;
    }

    @GetMapping("/methods")
    public List<MethodDefinition> methods() {
        return catalog.methods();
    }

    @GetMapping("/book-collections")
    public List<BookProtocolCollection> bookCollections() {
        return catalog.collections();
    }

    @PostMapping("/method-matches")
    public List<MethodMatch> match(@Valid @RequestBody MatchRequest request) {
        return engine.match(new MatchingInput(
                request.goalDomain(), request.desiredOutcome().trim(), request.obstacle(), request.availableMinutes(),
                request.highScreenTime(), request.structureLevel(), request.pastFailure() == null ? "" : request.pastFailure().trim()
        ));
    }

    public record MatchRequest(
            @NotNull GoalDomain goalDomain,
            @NotBlank String desiredOutcome,
            @NotNull ObstacleType obstacle,
            @Min(5) @Max(240) int availableMinutes,
            boolean highScreenTime,
            @NotNull StructureLevel structureLevel,
            String pastFailure
    ) {}
}
