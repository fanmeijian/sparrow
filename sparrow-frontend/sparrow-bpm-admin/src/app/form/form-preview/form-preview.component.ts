import { HttpClient } from '@angular/common/http';
import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Inject, NgZone, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { map, of } from 'rxjs';
import { environment } from 'src/environments/environment';
import JSON5 from 'json5';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-form-preview',
  templateUrl: './form-preview.component.html',
  styleUrls: ['./form-preview.component.css']
})
export class FormPreviewComponent implements AfterViewInit {
  form = new UntypedFormGroup({});
  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    schema: [null, Validators.required],
    remark: [null, Validators.required],
  })
  editor: any
  @ViewChild('editorContainer', { static: true }) editorContainer!: ElementRef<HTMLDivElement>;
  model = {};
  fields: FormlyFieldConfig[] = [

  ];
showFiller: any;
  constructor(
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private route: ActivatedRoute,
    private zone: NgZone,
    private formBuilder: UntypedFormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) { }
  ngAfterViewInit(): void {
    console.log(this.data)
    const id = this.route.snapshot.queryParamMap.get('id')
    const $formDesign = id ? this.http.get(`${environment.bpmApi}/forms/${id}`).pipe(map((m: any) => m.schema)) : of(this.data)
    $formDesign.subscribe((schema: any) => {

      this.loadMonacoViaCDN().then(() => {
        this.zone.runOutsideAngular(() => {
          (window as any).require(['vs/editor/editor.main'], () => {
            const monaco = (window as any).monaco;
            this.editor = monaco.editor.create(this.editorContainer.nativeElement, {
              value: `${JSON5.stringify(schema, null, 2)}`,
              language: 'javascript',
              theme: 'vs-dark',
              automaticLayout: true,
            });

            this.fields = schema
            // 监听变化
            this.editor.onDidChangeModelContent(() => {
              const newValue = this.editor.getValue();
              console.log('变更后的内容:', newValue);
              this.fields = JSON5.parse(newValue)
              this.formGroup.patchValue({ schema: JSON5.parse(newValue) })
            });

          });
        });
      });

    })
  }

  private loadMonacoViaCDN(): Promise<void> {
    return new Promise((resolve, reject) => {
      if ((window as any).monaco) {
        return resolve(); // already loaded
      }

      // Load loader.min.js
      const loaderScript = document.createElement('script');
      loaderScript.src = 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.26.1/min/vs/loader.min.js';
      loaderScript.onload = () => {
        (window as any).require.config({
          paths: {
            vs: 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.26.1/min/vs',
          },
        });
        resolve();
      };
      loaderScript.onerror = (err) => reject(err);

      document.body.appendChild(loaderScript);
    });
  }

  onSubmit(arg0: any) {
    console.log(this.model);
  }
}
