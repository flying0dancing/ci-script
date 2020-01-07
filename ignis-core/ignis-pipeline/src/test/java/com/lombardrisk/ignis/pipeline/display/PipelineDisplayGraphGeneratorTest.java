package com.lombardrisk.ignis.pipeline.display;

import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.pipeline.TransformationStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PipelineDisplayGraphGeneratorTest {

    private final PipelineDisplayGraphGenerator<String, TestStep> generator = new PipelineDisplayGraphGenerator<>();

    @Test
    public void generateDisplayGraph_OneToOneStep_ReturnsGraphWithSingleEdge() {
        TestStep mapAToB = new TestStep("map", singleton("A"), "B");

        PipelineDisplayGraph<String, TestStep> graph =
                VavrAssert.assertValid(generator.generateDisplayGraph(singleton(mapAToB)))
                        .getResult();

        assertThat(graph.edges())
                .containsOnly(new PipelineEdge<>("A", "B", mapAToB));
    }

    @Test
    public void generateDisplayGraph_ManyToOneStep_ReturnsGraphWithEdgesForEachJoin() {
        TestStep manyToOne = new TestStep("manyToOne", newHashSet("A", "B", "C", "D"), "E");

        PipelineDisplayGraph<String, TestStep> graph =
                VavrAssert.assertValid(generator.generateDisplayGraph(singleton(manyToOne)))
                        .getResult();

        assertThat(graph.edges())
                .containsOnly(
                        new PipelineEdge<>("A", "E", manyToOne),
                        new PipelineEdge<>("B", "E", manyToOne),
                        new PipelineEdge<>("C", "E", manyToOne),
                        new PipelineEdge<>("D", "E", manyToOne));
    }

    @Test
    public void generateDisplayGraph_MultipleStep_ReturnsGraphWithEdgesForAllSteps() {
        TestStep abcdToE = new TestStep("manyToOne", newHashSet("A", "B", "C", "D"), "E");
        TestStep mapEToF = new TestStep("map", singleton("E"), "F");
        TestStep gAndFToH = new TestStep("manyToOne", newHashSet("G", "F"), "H");

        PipelineDisplayGraph<String, TestStep> graph =
                VavrAssert.assertValid(generator.generateDisplayGraph(newHashSet(abcdToE, mapEToF, gAndFToH)))
                        .getResult();

        assertThat(graph.edges())
                .containsOnly(
                        new PipelineEdge<>("A", "E", abcdToE),
                        new PipelineEdge<>("B", "E", abcdToE),
                        new PipelineEdge<>("C", "E", abcdToE),
                        new PipelineEdge<>("D", "E", abcdToE),
                        new PipelineEdge<>("E", "F", mapEToF),
                        new PipelineEdge<>("F", "H", gAndFToH),
                        new PipelineEdge<>("G", "H", gAndFToH));
    }

    @Test
    public void generateDisplayGraph_DisconnectedGraph_ReturnsGraphMultipleConnectedSets() {
        TestStep abcdToE = new TestStep("manyToOne", newHashSet("A", "B", "C", "D"), "E");
        TestStep mapEToF = new TestStep("map", singleton("F"), "G");

        PipelineDisplayGraph<String, TestStep> graph =
                VavrAssert.assertValid(generator.generateDisplayGraph(newHashSet(abcdToE, mapEToF)))
                        .getResult();

        assertThat(graph.edges())
                .containsOnly(
                        new PipelineEdge<>("A", "E", abcdToE),
                        new PipelineEdge<>("B", "E", abcdToE),
                        new PipelineEdge<>("C", "E", abcdToE),
                        new PipelineEdge<>("D", "E", abcdToE),
                        new PipelineEdge<>("F", "G", mapEToF));

        List<Set<PipelineEdge<String, TestStep>>> connectedSets = graph.connectedSets();

        assertThat(connectedSets)
                .hasSize(2);
        assertThat(connectedSets)
                .containsOnly(
                        newHashSet(
                                new PipelineEdge<>("A", "E", abcdToE),
                                new PipelineEdge<>("B", "E", abcdToE),
                                new PipelineEdge<>("C", "E", abcdToE),
                                new PipelineEdge<>("D", "E", abcdToE)),
                        Collections.singleton(new PipelineEdge<>("F", "G", mapEToF)));
    }

    @Test
    public void generateDisplayGraph_CyclicalGraph_ReturnsError() {
        TestStep aToB = new TestStep("aToB", singleton("A"), "B");
        TestStep bToC = new TestStep("bToC", singleton("B"), "C");
        TestStep cToA = new TestStep("cToA", singleton("C"), "A");

        PipelineCycleError<String, TestStep> failure = VavrAssert.assertFailed(
                generator.generateDisplayGraph(newLinkedHashSet(Arrays.asList(aToB, bToC, cToA))))
                .getValidation();

        assertThat(failure.getParentChain())
                .containsOnly("A", "B");
        assertThat(failure.getCyclicalStep())
                .isEqualTo(cToA);
    }

    @Data
    @AllArgsConstructor
    private static class TestStep implements TransformationStep<String> {

        private final String name;
        private final Set<String> inputs;
        private final String output;
    }
}


