import { ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import { EditorUtils } from '../../core/forms/editor-utils';
import { ReactiveFormUtilities } from '../../core/forms/reactive-form.utils';
import { Schema } from '../../schemas';
import ICodeEditor = monaco.editor.ICodeEditor;
import IEditorConstructionOptions = monaco.editor.IEditorConstructionOptions;

@Component({
  selector: 'dv-pipeline-select-editor',
  styleUrls: ['./pipeline-select-editor.component.scss'],
  templateUrl: './pipeline-select-editor.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineSelectEditorComponent implements OnInit, OnChanges, OnDestroy {
  @Input() editorLabel: string;

  @Input() editorControl: FormControl;

  @Input() schemas: Schema[];

  @Input() height = 4;

  editorHeight;
  @Input()
  set editorHeightSetter(val: number) {
    this.editorHeight = val + 'px';
  }


  @Input() error?: boolean;

  public isRequired: boolean;

  private editor: ICodeEditor;

  private controlSubscriptions: Subscription[] = [];

  editorOptions: IEditorConstructionOptions = {
    language: EditorUtils.sparkSqlId
  };

  ngOnInit(): void {
    if (this.editorHeight === undefined) {
      this.editorHeight = EditorUtils.createEditorHeight(this.height);
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    const { schemas, editorControl } = changes;

    if (schemas) {
      this.updateCompletionData(schemas.currentValue || [], this.editor);
    }

    if (editorControl) {
      this.handleEditorControlChange(editorControl.currentValue);
    }
  }

  ngOnDestroy() {
    this.unsubscribeEditorControlSubscriptions();
  }

  handleEditorInit(editor: ICodeEditor): void {
    this.editor = editor;

    this.updateCompletionData(this.schemas, editor);

    editor.onDidDispose(() => {
      EditorUtils.clearSparkSqlEditorInstanceFields(this.editor.getModel());

      this.editor = null;
    });
  }

  handleEditorControlChange(editorControl: FormControl) {
    if (!editorControl) {
      return this.unsubscribeEditorControlSubscriptions();
    }

    this.isRequired = ReactiveFormUtilities.hasRequiredField(editorControl);
    this.updateEditorDisabled(this.editor, editorControl.status);

    this.controlSubscriptions.push(
      editorControl.statusChanges.subscribe(status => {
        this.isRequired = ReactiveFormUtilities.hasRequiredField(editorControl);
        this.updateEditorDisabled(this.editor, status);
      })
    );
  }

  refreshEditorControlState() {
    this.isRequired = ReactiveFormUtilities.hasRequiredField(
      this.editorControl
    );

    this.updateEditorDisabled(this.editor, this.editorControl.status);
  }

  private updateCompletionData(schemas: Schema[], editor: ICodeEditor) {
    if (!schemas || !editor) return;

    EditorUtils.setSparkSqlEditorInstanceFields(editor.getModel(), schemas);
  }

  private updateEditorDisabled(editor: ICodeEditor, status: string) {
    if (!editor) return;

    editor.updateOptions({ readOnly: status === 'DISABLED' });
  }

  private unsubscribeEditorControlSubscriptions() {
    this.controlSubscriptions.forEach(s => s.unsubscribe());

    this.controlSubscriptions = [];
  }
}
