package com.lombardrisk.ignis.pipeline;

import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Either;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PipelinePlanGeneratorTest {

    private PipelinePlanGenerator<Mock.Dataset, Mock.PipelineStep> generator = new PipelinePlanGenerator<>();

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void generateExecutionPlan_SingleStep_GeneratesGraph() {
        Mock.PipelineStep theOnlyStep = Mock.PipelineStep.builder()
                .stepName("the only step")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(singleton(theOnlyStep)).build();

        PipelinePlan<Mock.Dataset, Mock.PipelineStep> pipelinePlan =
                VavrAssert.assertValid(generator.generateExecutionPlan(pipeline)).getResult();

        assertThat(pipelinePlan.getPipelineStepsInOrderOfExecution())
                .containsExactly(theOnlyStep);

        assertThat(pipelinePlan.getStepDependencies(theOnlyStep))
                .containsExactly(Either.left(Mock.Dataset.withName("A")));

        assertThat(pipelinePlan.getRequiredPipelineInputs())
                .containsExactly(Mock.Dataset.withName("A"));
    }

    @Test
    public void generateExecutionPlan_PipelineStepsNull_GeneratesGraph() {
        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(null).build();

        PipelinePlan<Mock.Dataset, Mock.PipelineStep> pipelinePlan =
                VavrAssert.assertValid(generator.generateExecutionPlan(pipeline)).getResult();

        assertThat(pipelinePlan.getPipelineStepsInOrderOfExecution())
                .isEmpty();

        assertThat(pipelinePlan.getRequiredPipelineInputs())
                .isEmpty();
    }

    @Test
    public void generateExecutionPlan_PipelineStepsEmpty_GeneratesGraph() {
        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(emptySet()).build();

        PipelinePlan<Mock.Dataset, Mock.PipelineStep> pipelinePlan =
                VavrAssert.assertValid(generator.generateExecutionPlan(pipeline)).getResult();

        assertThat(pipelinePlan.getPipelineStepsInOrderOfExecution())
                .isEmpty();

        assertThat(pipelinePlan.getRequiredPipelineInputs())
                .isEmpty();
    }

    @Test
    public void generateExecutionPlan_SingleStepSelfJoin_ReturnsErrorMessage() {
        Mock.PipelineStep theOnlyStep = Mock.PipelineStep.builder()
                .stepName("a self-joining step")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("A"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(singleton(theOnlyStep)).build();

        List<ErrorResponse> errorResponses = VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                .getValidation()
                .getErrors();

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("PIPELINE_STEP_SELF_JOIN");

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorMessage)
                .containsExactly("Self-join on pipeline steps not allowed [a self-joining step]");
    }

    @Test
    public void generateExecutionPlan_SingleStepSelfJoin_ReturnsErrorObjectWithSelfJoiningStep() {
        Mock.PipelineStep theOnlyStep = Mock.PipelineStep.builder()
                .stepName("a self-joining step")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("A"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(singleton(theOnlyStep)).build();

        PipelinePlanError<Mock.Dataset, Mock.PipelineStep> errorResponses =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation();

        assertThat(errorResponses.getSelfJoiningSteps())
                .containsOnly(theOnlyStep);
    }

    @Test
    public void generateExecutionPlan_TwoStepsCircularDependency_ReturnsError() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .stepName("step 1")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .stepName("step 2")
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("A"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder()
                .steps(newHashSet(step1, step2))
                .build();

        List<ErrorResponse> errorResponses =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation()
                        .getErrors();

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("PIPELINE_GRAPH_CYCLICAL");
    }

    @Test
    public void generateExecutionPlan_CircularDependencyOneDegreeOfSeparation_ReturnsErrorMessage() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .stepName("one step")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .stepName("another step")
                .datasetIn(Mock.Dataset.withName("B")).datasetIn(Mock.Dataset.withName("E"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .stepName("a join step")
                .datasetIn(Mock.Dataset.withName("C")).datasetIn(Mock.Dataset.withName("D"))
                .datasetOut(Mock.Dataset.withName("A"))
                .build();

        Mock.PipelineStep step4 = Mock.PipelineStep.builder()
                .stepName("a whole other step")
                .datasetIn(Mock.Dataset.withName("Z"))
                .datasetOut(Mock.Dataset.withName("E"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder()
                .steps(newHashSet(step1, step2, step3, step4))
                .build();

        List<ErrorResponse> errorResponses =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation()
                        .getErrors();

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("PIPELINE_GRAPH_CYCLICAL");
    }

    @Test
    public void generateExecutionPlan_CircularDependencyOneDegreeOfSeparation_ReturnsErrorObjectWithHasCyclesFlag() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .stepName("one step")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .stepName("another step")
                .datasetIn(Mock.Dataset.withName("B")).datasetIn(Mock.Dataset.withName("E"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .stepName("a join step")
                .datasetIn(Mock.Dataset.withName("C")).datasetIn(Mock.Dataset.withName("D"))
                .datasetOut(Mock.Dataset.withName("A"))
                .build();

        Mock.PipelineStep step4 = Mock.PipelineStep.builder()
                .stepName("a whole other step")
                .datasetIn(Mock.Dataset.withName("Z"))
                .datasetOut(Mock.Dataset.withName("E"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder()
                .steps(newHashSet(step1, step2, step3, step4))
                .build();

        PipelinePlanError<Mock.Dataset, Mock.PipelineStep> error =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation();

        assertThat(error.isHasCycles())
                .isTrue();
    }

    @Test
    public void generateExecutionPlan_MultipleStepsOutputSameSchema_ReturnsErrorMessage() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .stepName("step 1")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .stepName("step 2")
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .stepName("step 3")
                .datasetIn(Mock.Dataset.withName("C"))
                .datasetOut(Mock.Dataset.withName("D"))
                .build();

        Mock.PipelineStep step4 = Mock.PipelineStep.builder()
                .stepName("step 4")
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("D"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder()
                .steps(newHashSet(step1, step2, step3, step4))
                .build();

        List<ErrorResponse> errorResponses =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation()
                        .getErrors();

        assertThat(errorResponses)
                .hasSize(1);

        assertThat(errorResponses.get(0).getErrorCode())
                .isEqualTo("NON_UNIQUE_OUTPUT_SCHEMA");

        assertThat(errorResponses.get(0).getErrorMessage())
                .contains("step 3", "step 4")
                .doesNotContain("step 1, step 2");
    }

    @Test
    public void generateExecutionPlan_MultipleStepsOutputSameSchema_ReturnsErrorObjectsWithOffendingSteps() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .stepName("step 1")
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .stepName("step 2")
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .stepName("step 3")
                .datasetIn(Mock.Dataset.withName("C"))
                .datasetOut(Mock.Dataset.withName("D"))
                .build();

        Mock.PipelineStep step4 = Mock.PipelineStep.builder()
                .stepName("step 4")
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("D"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder()
                .steps(newHashSet(step1, step2, step3, step4))
                .build();

        PipelinePlanError<Mock.Dataset, Mock.PipelineStep> error =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation();

        assertThat(error.getStepsToSameOutputSchemas())
                .hasSize(1);

        assertThat(error.getStepsToSameOutputSchemas().get(0).getSteps())
                .containsOnly(step3, step4);
        assertThat(error.getStepsToSameOutputSchemas().get(0).getOutput())
                .isEqualTo(Mock.Dataset.withName("D"));
    }

    @Test
    public void generateExecutionPlan_WithMultipleSteps_GeneratesGraph() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("C"))
                .datasetOut(Mock.Dataset.withName("D"))
                .build();

        Mock.PipelineStep step4 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("X"))
                .datasetIn(Mock.Dataset.withName("Y"))
                .datasetIn(Mock.Dataset.withName("C"))
                .datasetIn(Mock.Dataset.withName("D"))
                .datasetOut(Mock.Dataset.withName("E"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(newHashSet(step1, step2, step3, step4)).build();

        PipelinePlan<Mock.Dataset, Mock.PipelineStep> pipelinePlan =
                VavrAssert.assertValid(generator.generateExecutionPlan(pipeline)).getResult();

        assertThat(pipelinePlan.getPipelineStepsInOrderOfExecution())
                .containsExactly(step1, step2, step3, step4);

        assertThat(pipelinePlan.getStepDependencies(step1))
                .containsExactly(Either.left(Mock.Dataset.withName("A")));

        assertThat(pipelinePlan.getStepDependencies(step2))
                .containsExactly(Either.right(step1));

        assertThat(pipelinePlan.getStepDependencies(step3))
                .containsExactly(Either.right(step2));

        assertThat(pipelinePlan.getStepDependencies(step4))
                .containsExactlyInAnyOrder(
                        Either.left(Mock.Dataset.withName("X")),
                        Either.left(Mock.Dataset.withName("Y")),
                        Either.right(step2),
                        Either.right(step3));

        assertThat(pipelinePlan.getRequiredPipelineInputs())
                .containsExactlyInAnyOrder(
                        Mock.Dataset.withName("A"),
                        Mock.Dataset.withName("X"),
                        Mock.Dataset.withName("Y"));
    }

    @Test
    public void generateExecutionPlan_GraphNotConnected_ReturnsError() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("X"))
                .datasetIn(Mock.Dataset.withName("Y"))
                .datasetOut(Mock.Dataset.withName("E"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(newHashSet(step1, step2, step3)).build();

        List<ErrorResponse> errorResponses =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation()
                        .getErrors();

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("PIPELINE_GRAPH_NOT_CONNECTED");
    }

    @Test
    public void generateExecutionPlan_GraphNotConnected_ReturnsErrorWithConnectedSets() {
        Mock.PipelineStep step1 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("A"))
                .datasetOut(Mock.Dataset.withName("B"))
                .build();

        Mock.PipelineStep step2 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("B"))
                .datasetOut(Mock.Dataset.withName("C"))
                .build();

        Mock.PipelineStep step3 = Mock.PipelineStep.builder()
                .datasetIn(Mock.Dataset.withName("X"))
                .datasetIn(Mock.Dataset.withName("Y"))
                .datasetOut(Mock.Dataset.withName("E"))
                .build();

        Mock.Pipeline pipeline = Mock.Pipeline.builder().steps(newHashSet(step1, step2, step3)).build();

        PipelinePlanError<Mock.Dataset, Mock.PipelineStep> error =
                VavrAssert.assertFailed(generator.generateExecutionPlan(pipeline))
                        .getValidation();

        assertThat(error.getGraphNotConnected().getConnectedSets())
                .containsExactlyInAnyOrder(
                        newHashSet(
                                Mock.Dataset.withName("A"),
                                Mock.Dataset.withName("B"),
                                Mock.Dataset.withName("C")),
                        newHashSet(
                                Mock.Dataset.withName("X"),
                                Mock.Dataset.withName("Y"),
                                Mock.Dataset.withName("E")));
    }
}
