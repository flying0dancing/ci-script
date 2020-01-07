import {
  findSchemas,
  Pipeline,
  PipelineEdge,
  PipelineInvocation,
  PipelineStepInvocation
} from "@/core/api/pipelines/pipelines.interfaces";
import { PipelinesService } from "@/core/api/pipelines/pipelines.service";
import {
  EdgeContext,
  EdgeKey,
  NodeContext
} from "@/core/dag/dag-context.interface";
import { DagEventService } from "@/core/dag/dag-event.service";
import { PipelineSelectService } from "@/fcr/drillback/pipeline-select.service";
import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges
} from "@angular/core";
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators
} from "@angular/forms";
import * as moment from "moment";
import { Subscription } from "rxjs";
import * as d3 from "d3";

@Component({
  selector: "app-drillback-menu",
  styleUrls: ["./drillback-menu.component.scss"],
  templateUrl: "./drillback-menu.component.html"
})
export class DrillbackMenuComponent implements OnInit, OnDestroy, OnChanges {
  @Input()
  pipelineInvocationId: number;
  @Input()
  pipelineStepInvocationId: number;
  @Input()
  pipelineInvocations: PipelineInvocation[] = [];
  @Input()
  pipelines: Pipeline[] = [];
  @Input()
  loading = false;
  @Input()
  minimised = false;

  formGroup: FormGroup = this.formBuilder.group({
    selectedPipelineId: null,
    selectedPipelineInvocationId: null,
    selectedPipelineStepInvocationId: null,
    entityCode: [null, Validators.required],
    referenceDate: [null, Validators.required]
  });

  selectedPipelineInvocation: PipelineInvocation = null;
  selectedPipelineStepInvocation: PipelineStepInvocation = null;
  filteredPipelineInvocations: PipelineInvocation[] = [];

  selectedPipelineEdges: PipelineEdge[] = [];
  schemaNodeContext: Map<number, NodeContext> = new Map<number, NodeContext>();
  edgeContextMap: Map<EdgeKey, EdgeContext> = new Map<EdgeKey, EdgeContext>();
  dagZoom = 1.0;
  minDagZoom = 0.9;
  maxDagZoom = 3.0;
  colorMapOverride: Map<number, string> = new Map<number, string>();

  private selectedPipelineIdControl: AbstractControl = this.formGroup.get(
    "selectedPipelineId"
  );
  private selectedPipelineInvocationIdControl: AbstractControl = this.formGroup.get(
    "selectedPipelineInvocationId"
  );
  private selectedPipelineStepInvocationIdControl: AbstractControl = this.formGroup.get(
    "selectedPipelineStepInvocationId"
  );
  private referenceDateControl: AbstractControl = this.formGroup.get(
    "referenceDate"
  );
  private entityCodeControl: AbstractControl = this.formGroup.get("entityCode");

  private selectedPipelineInvocationIdSubscription: Subscription;
  private selectedPipelineStepInvocationIdSubscription: Subscription;
  private selectedEdgeSubscription: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private pipelineSelectService: PipelineSelectService,
    private pipelinesService: PipelinesService,
    private dagEventService: DagEventService
  ) {}

  ngOnInit(): void {
    this.setupPipelineInvocationSelection();
    this.setupReferenceDateSelection();
    this.setupEntityCodeSelection();
    this.setupPipelineSelection();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.loading) {
      this.selectedPipelineInvocationIdControl.disable();
    } else {
      this.selectedPipelineInvocationIdControl.enable();
    }

    if (this.pipelineInvocationId) {
      const pipelineInvc = this.pipelineInvocations.find(
        pipelineInvocation =>
          pipelineInvocation.id === this.pipelineInvocationId
      );

      if (!!pipelineInvc) {
        this.entityCodeControl.setValue(pipelineInvc.entityCode);
        this.referenceDateControl.setValue(pipelineInvc.referenceDate);
        this.selectedPipelineIdControl.setValue(pipelineInvc.pipelineId);
        this.selectedPipelineInvocationIdControl.setValue(pipelineInvc.id);
      }
    }

    if (this.pipelineStepInvocationId) {
      this.selectedPipelineStepInvocationIdControl.setValue(
        this.pipelineStepInvocationId
      );
    }
  }

  private setupPipelineInvocationSelection(): void {
    this.selectedPipelineInvocationIdSubscription = this.selectedPipelineInvocationIdControl.valueChanges.subscribe(
      pipelineInvocationId =>
        this.handlePipelineInvocationSelection(pipelineInvocationId)
    );

    this.selectedPipelineStepInvocationIdSubscription = this.selectedPipelineStepInvocationIdControl.valueChanges.subscribe(
      pipelineStepInvocationId =>
        this.handlePipelineStepInvocationSelection(pipelineStepInvocationId)
    );

    this.selectedEdgeSubscription = this.dagEventService.selectEdge.subscribe(
      edgeId => this.handleDagEdgeSelection(edgeId)
    );
  }

  private handlePipelineInvocationSelection(
    pipelineInvocationId: number
  ): void {
    this.selectedPipelineInvocation = this.filteredPipelineInvocations.find(
      invocation => invocation.id === pipelineInvocationId
    );

    if (this.selectedPipelineInvocation) {
      const firstInvocationStep = this.selectedPipelineInvocation
        .invocationSteps[0];

      this.pipelineSelectService.pipelineInvocationSelectEvent.emit({
        pipelineInvocation: this.selectedPipelineInvocation,
        pipelineStepInvocation: firstInvocationStep
      });

      this.selectedPipelineStepInvocationIdControl.setValue(
        firstInvocationStep.id
      );
      this.selectedPipelineStepInvocationIdControl.markAsDirty();
      this.extractSchemasToNodeContext(this.selectedPipelineInvocation);
      this.retrievePipelineEdges(this.selectedPipelineInvocation);
    } else {
      this.selectedPipelineStepInvocationIdControl.setValue(null);
    }
  }

  private retrievePipelineEdges(pipelineInvocation: PipelineInvocation) {
    const edgesSubscription: Subscription = this.pipelinesService
      .getPipelineEdges(pipelineInvocation.pipelineId)
      .subscribe((edges: PipelineEdge[]) => {
        this.selectedPipelineEdges = edges;
        this.extractEdgeContext(edges);
        edgesSubscription.unsubscribe();
      });
  }

  private extractEdgeContext(edges: PipelineEdge[]) {
    for (const edge of edges) {
      const key = edge.source + "-" + edge.target;

      const validEdges = this.selectedPipelineInvocation.invocationSteps.map(
        invocationStep => ({
          edge: invocationStep.pipelineStep.schemaIn
            ? invocationStep.pipelineStep.schemaIn.id +
              "-" +
              invocationStep.pipelineStep.schemaOut.id
            : invocationStep.pipelineStep.schemaOut.id,
          valid: invocationStep.status === "SUCCESS" ? true : false
        })
      );

      let foundValidEdge = validEdges.find(obj => obj.edge === key);
      if (!foundValidEdge) {
        foundValidEdge = validEdges.find(obj => obj.edge === edge.target);
      }
      this.edgeContextMap[key] = {
        tooltip: foundValidEdge.valid
          ? `${edge.pipelineStep.type} Step - ${edge.pipelineStep.name}`
          : `${edge.pipelineStep.type} Step - ${edge.pipelineStep.name} - Did not run`,
        id: edge.pipelineStep.id,
        valid: foundValidEdge.valid
      };
    }
  }

  mousewheelOnDagContainer($event: WheelEvent) {
    $event.preventDefault();

    const zoomDirection = Math.sign($event.deltaY);
    if (zoomDirection > 0 && this.dagZoom > this.maxDagZoom) {
      return;
    }

    if (zoomDirection < 0 && this.dagZoom < this.minDagZoom) {
      return;
    }
    this.dagZoom += 0.1 * zoomDirection;
  }

  private handlePipelineStepInvocationSelection(
    stepInvocationId: number
  ): void {
    if (this.selectedPipelineInvocation) {
      this.selectedPipelineStepInvocation = this.selectedPipelineInvocation.invocationSteps.find(
        stepInvocation => stepInvocation.id === stepInvocationId
      );

      this.dagEventService.selectEdge.emit(
        this.selectedPipelineStepInvocation.pipelineStep.id
      );
      this.pipelineSelectService.pipelineInvocationSelectEvent.emit({
        pipelineInvocation: this.selectedPipelineInvocation,
        pipelineStepInvocation: this.selectedPipelineStepInvocation
      });
    }
  }

  private handleDagEdgeSelection(stepId: number) {
    this.selectedPipelineStepInvocation = this.selectedPipelineInvocation.invocationSteps.find(
      stepInvocation => stepInvocation.pipelineStep.id === stepId
    );

    this.pipelineSelectService.pipelineInvocationSelectEvent.emit({
      pipelineInvocation: this.selectedPipelineInvocation,
      pipelineStepInvocation: this.selectedPipelineStepInvocation
    });
  }

  private setupReferenceDateSelection(): void {
    this.referenceDateControl.valueChanges.subscribe(() => {
      this.filterPipelineInvocations();
    });
  }

  private setupEntityCodeSelection(): void {
    this.entityCodeControl.valueChanges.subscribe(() => {
      this.filterPipelineInvocations();
    });
  }

  private setupPipelineSelection(): void {
    this.selectedPipelineIdControl.valueChanges.subscribe(() => {
      this.filterPipelineInvocations();
    });
  }

  private filterPipelineInvocations() {
    const entityCode = this.entityCodeControl.value;
    const referenceDate = this.referenceDateControl.value;
    const pipelineId = this.selectedPipelineIdControl.value;
    const refDateString = moment(referenceDate).format("YYYY-MM-DD");

    this.filteredPipelineInvocations = this.pipelineInvocations.filter(
      invocation =>
        invocation.entityCode === entityCode &&
        invocation.referenceDate === refDateString &&
        invocation.pipelineId === pipelineId
    );

    const pipelineInvocationId = this.selectedPipelineInvocationIdControl.value;
    if (
      this.filteredPipelineInvocations
        .map(pip => pip.id)
        .indexOf(pipelineInvocationId) === -1
    ) {
      this.selectedPipelineInvocationIdControl.setValue(null);
      this.selectedPipelineInvocationIdControl.markAsDirty();
    }
  }

  ngOnDestroy(): void {
    this.selectedPipelineInvocationIdSubscription.unsubscribe();
    this.selectedPipelineStepInvocationIdSubscription.unsubscribe();
    this.selectedEdgeSubscription.unsubscribe();
  }

  minimise() {
    this.minimised = true;
  }

  maximise() {
    this.minimised = false;
  }

  private extractSchemasToNodeContext(pipelineInvocation: PipelineInvocation) {
    for (const step of pipelineInvocation.invocationSteps) {
      const schemas = findSchemas(step.pipelineStep);
      for (const schema of schemas) {
        this.setSchemaNodeContext(schema, step);
        if (this.isStepInvalid(step, schema)) {
          this.colorMapOverride[schema.id] = d3.schemePastel1[9];
        }
      }
    }
  }

  private setSchemaNodeContext(schema, step) {
    let isNodeValid = false;
    if (
      typeof this.schemaNodeContext[schema.id] !== "undefined" &&
      this.schemaNodeContext[schema.id].valid === true
    ) {
      isNodeValid = true;
    }
    this.schemaNodeContext[schema.id] = {
      tooltip: schema.displayName,
      valid: !isNodeValid ? this.isNodeValid(step, schema) : isNodeValid
    };
  }

  private isNodeValid(step, schema) {
    return step.status === "SUCCESS";
  }

  private isStepInvalid(step, schema) {
    return (
      step.status !== "SUCCESS" &&
      schema.physicalTableName === step.pipelineStep.schemaOut.physicalTableName
    );
  }
}
