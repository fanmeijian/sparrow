import { NgForOf } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, NgZone, OnInit, ViewChild } from '@angular/core';
import { FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { CompactType, DisplayGrid, GridsterComponent, GridsterConfig, GridsterItem, GridsterItemComponent, GridType } from 'angular-gridster2';
import JSON5 from 'json5';
import { map, of } from 'rxjs';
import { AppComponent } from 'src/app/app.component';
import { environment } from 'src/environments/environment';


@Component({
  selector: 'app-form-design',
  templateUrl: './form-design.component.html',
  styleUrls: ['./form-design.component.css'],
})
export class FormDesignComponent implements OnInit, AfterViewInit {
  options!: GridsterConfig | any;
  dashboard: any;
  saveForm() {
    this.formGroup.markAllAsTouched();
    console.log(this.formGroup.value)
    if (this.formGroup.invalid) {
      this.snack.open('请核实表单设计内容', '关闭')
      return
    }
    const body = this.formGroup.value
    this.http.post(`${environment.bpmApi}/forms`, body).subscribe(() => {
      this.router.navigate(['..'], { relativeTo: this.route })
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
    this.options = {
      gridType: GridType.Fit,
      displayGrid: DisplayGrid.Always,
      enableEmptyCellClick: false,
      enableEmptyCellContextMenu: false,
      enableEmptyCellDrop: false,
      enableEmptyCellDrag: false,
      enableOccupiedCellDrop: false,
      emptyCellClickCallback: this.emptyCellClick.bind(this),
      emptyCellContextMenuCallback: this.emptyCellClick.bind(this),
      emptyCellDropCallback: this.emptyCellClick.bind(this),
      emptyCellDragCallback: this.emptyCellClick.bind(this),
      emptyCellDragMaxCols: 50,
      emptyCellDragMaxRows: 50
    };

    this.dashboard = [
      { cols: 2, rows: 1, y: 0, x: 0 },
      { cols: 2, rows: 2, y: 0, x: 2 },
      { cols: 1, rows: 1, y: 0, x: 4 },
      { cols: 3, rows: 2, y: 1, x: 4 },
      { cols: 1, rows: 1, y: 4, x: 5 },
      { cols: 1, rows: 1, y: 2, x: 1 },
      { cols: 2, rows: 2, y: 5, x: 5 },
      { cols: 2, rows: 2, y: 3, x: 2 },
      { cols: 2, rows: 1, y: 2, x: 2 },
      { cols: 1, rows: 1, y: 3, x: 4 },
      { cols: 1, rows: 1, y: 0, x: 6 }
    ];
  }

  ngAfterViewInit(): void {
    const id = this.route.snapshot.queryParamMap.get('id')
    const $formDesign = id ? this.http.get(`${environment.bpmApi}/forms/${id}`).pipe(map((m: any) => m.schema)) : of([])
    // $formDesign.subscribe((schema: any) => {

    //   this.loadMonacoViaCDN().then(() => {
    //     this.zone.runOutsideAngular(() => {
    //       (window as any).require(['vs/editor/editor.main'], () => {
    //         const monaco = (window as any).monaco;
    //         this.editor = monaco.editor.create(this.editorContainer.nativeElement, {
    //           value: `${JSON5.stringify(schema, null, 2)}`,
    //           language: 'javascript',
    //           theme: 'vs-dark',
    //           automaticLayout: true,
    //         });

    //         this.fields = schema
    //         // 监听变化
    //         this.editor.onDidChangeModelContent(() => {
    //           const newValue = this.editor.getValue();
    //           console.log('变更后的内容:', newValue);
    //           this.fields = JSON5.parse(newValue)
    //           this.formGroup.patchValue({ schema: JSON5.parse(newValue) })
    //         });

    //       });
    //     });
    //   });

    // })

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


  changedOptions(): void {
    if (this.options.api && this.options.api.optionsChanged) {
      this.options.api.optionsChanged();
    }
  }

  emptyCellClick(event: MouseEvent, item: GridsterItem): void {
    console.info('empty cell click', event, item);
    this.dashboard.push(item);
  }

  removeItem($event: MouseEvent | TouchEvent, item: any): void {
    $event.preventDefault();
    $event.stopPropagation();
    this.dashboard.splice(this.dashboard.indexOf(item), 1);
  }

  addItem(): void {
    this.dashboard.push({ x: 0, y: 0, cols: 1, rows: 1 });
  }

  dragStartHandler(ev: DragEvent): void {
    if (ev.dataTransfer) {
      ev.dataTransfer.setData('text/plain', 'Drag Me Button');
      ev.dataTransfer.dropEffect = 'copy';
    }
  }


  options1: GridsterConfig = {
    draggable: { enabled: true },
    resizable: { enabled: true },
    pushItems: true,
    minCols: 6,
    minRows: 6,
  };

  layout: Array<GridsterItem & { field?: FormlyFieldConfig }> = [];

  availableFields: FormlyFieldConfig[] = [
    {
      key: 'input',
      type: 'input',
      templateOptions: { label: '输入框', placeholder: '请输入', required: true }
    },
    {
      key: 'select',
      type: 'select',
      templateOptions: {
        label: '选择框', options: [
          { value: 1, label: '选项1' },
          { value: 2, label: '选项2' }
        ]
      }
    }
  ];

  form1 = new FormGroup({});
  model1: any = {};
  fields1: FormlyFieldConfig[] = [];

  addFieldToGrid(field: FormlyFieldConfig) {
    const key = field.key + '_' + Date.now();
    const newField = { ...field, key };

    this.fields1.push(newField); // 注册到 Formly 表单
    this.layout.push({
      cols: 2,
      rows: 1,
      y: 0,
      x: 0,
      field: newField
    });
  }

  submit() {
    console.log('表单提交:', this.model);
  }



  form2 = new FormGroup({});
  model2: any = {};
  fields2: FormlyFieldConfig[] = [
    {
      key: 'grid',
      fieldGroup: [],
      templateOptions: {
        layout: []
      }
    }
  ];


}
