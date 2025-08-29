import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, NgZone, ViewChild } from '@angular/core';
import { FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { GridsterConfig, GridsterItem } from 'angular-gridster2';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { map, of } from 'rxjs';
import { environment } from 'src/environments/environment';
import JSON5 from 'json5';
import { MatDialog } from '@angular/material/dialog';
import { FormPreviewComponent } from '../form-preview/form-preview.component';
import { GristerFieldTypeComponent, ON_ITEM_ADD } from '../grister-field-type/grister-field-type.component';

@Component({
  selector: 'app-grister-test',
  templateUrl: './grister-test.component.html',
  styleUrls: ['./grister-test.component.css']
})
export class GristerTestComponent implements AfterViewInit {
  onChange($event: any) {
    throw new Error('Method not implemented.');
  }
  editorOptions: any = {
    value: ``,
    language: 'javascript',
    theme: 'vs-dark',
    automaticLayout: true,
  };
  code: string = `sddfs`;
  preview() {
    this.dialog.open(FormPreviewComponent, { width: '100%', data: this.gridsterField.fieldGroup });
  }
  constructor(
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private route: ActivatedRoute,
    private zone: NgZone,
    private formBuilder: UntypedFormBuilder,
    private dialog: MatDialog,
  ) {
    ON_ITEM_ADD.subscribe(res => {
      if (res) {
        console.log(res)
        this.addInput(res)
      }

    })
  }

  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    schema: [null, Validators.required],
    remark: [null, Validators.required],
  })
  editor: any
  @ViewChild('editorContainer', { static: true }) editorContainer!: ElementRef<HTMLDivElement>;
  ngAfterViewInit(): void {

  }
  form = new FormGroup({});
  model: any = {};
  formlyOptions: FormlyFormOptions = {};
  fields: FormlyFieldConfig[] = [
    {
      key: 'gridster',
      type: 'gridster',
      form: this.form,
      fieldGroup: [],
    },
  ];

  get gridsterField() {
    return this.fields[0];
  }

  dragStartHandler(ev: DragEvent, type: string): void {
    if (ev.dataTransfer) {
      ev.dataTransfer.setData('text/plain', 'Drag Me Button');
      ev.dataTransfer.setData('type', type);
      ev.dataTransfer.dropEffect = 'copy';
    }
  }



  addInput(item: any) {
    const key = 'input_' + Date.now();
    const newField: any = {
      key,
      type: item.type,
      props: {
        label: 'Email address1234',
        placeholder: 'Enter email',
        required: true,
        gristerItem: Object.assign(item.item, {cols:3})
      },

    };

    this.gridsterField.fieldGroup = [
      ...(this.gridsterField.fieldGroup || []),
      newField
    ];

    this.model = { ...this.model, [key]: '' };
    this.code = JSON5.stringify(this.gridsterField.fieldGroup,undefined,'\t')

  }

  saveLayout() {
    const data = this.gridsterField.fieldGroup!.map(f => ({
      key: f.key,
      type: f.type,
      templateOptions: { ...f.templateOptions },
      validation: f.validation || {},
    }));

    localStorage.setItem('gridster-formly-layout', JSON.stringify(data));
    alert('布局已保存');
  }

  loadLayout() {
    const saved = localStorage.getItem('gridster-formly-layout');
    if (!saved) {
      alert('无保存布局');
      return;
    }
    const data = JSON.parse(saved);

    this.gridsterField.fieldGroup = [];
    this.model = {};
    data.forEach((f: any) => {
      this.gridsterField.fieldGroup!.push(f);
      this.model[f.key] = '';
    });
    alert('布局已加载');
  }

  submit() {
    console.log('表单数据', this.model);
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
