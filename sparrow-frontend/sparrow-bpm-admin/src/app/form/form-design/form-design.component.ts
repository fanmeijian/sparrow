import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, NgZone, OnInit, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { FormlyFieldConfig } from '@ngx-formly/core';
import JSON5 from 'json5';
import { map, of } from 'rxjs';
import { environment } from 'src/environments/environment';


@Component({
  selector: 'app-form-design',
  templateUrl: './form-design.component.html',
  styleUrls: ['./form-design.component.css']
})
export class FormDesignComponent implements OnInit, AfterViewInit {
  saveForm() {
    this.formGroup.markAllAsTouched();
    console.log(this.formGroup.value)
    if (this.formGroup.invalid) {
      this.snack.open('请核实表单设计内容','关闭')
      return
    }
    const body = this.formGroup.value
    this.http.post(`${environment.bpmApi}/forms`, body).subscribe(()=>{
      this.router.navigate(['..'], {relativeTo: this.route})
    })
  }


  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    schema: [null, Validators.required],
    remark: [null, Validators.required],
  })

  editor: any
  onSubmit(arg0: any) {
    console.log(this.model);
  }
  form = new UntypedFormGroup({});
  model = {};
  fields: FormlyFieldConfig[] = [

  ];

  @ViewChild('editorContainer', { static: true }) editorContainer!: ElementRef<HTMLDivElement>;

  constructor(
    private zone: NgZone,
    private formBuilder: UntypedFormBuilder,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private snack: MatSnackBar,
  ) { }
  ngOnInit(): void {

  }

  ngAfterViewInit(): void {
    const id = this.route.snapshot.queryParamMap.get('id')
    const $formDesign = id ? this.http.get(`${environment.bpmApi}/forms/${id}`).pipe(map((m: any) => m.schema)) : of([])
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
}
