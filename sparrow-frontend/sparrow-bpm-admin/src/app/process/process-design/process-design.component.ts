import { HttpClient, HttpParams } from '@angular/common/http';
import { AfterViewInit, Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom, map, of, switchMap, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
///@ts-ignore
import * as BpmnEditor from 'src/assets/bpmn/index.js'
import { EditorApi, EditorStandaloneResource } from 'src/app/types/common/Editor'


@Component({
  selector: 'app-process-design',
  templateUrl: './process-design.component.html',
  styleUrls: ['./process-design.component.css'],

})
export class ProcessDesignComponent implements OnInit, AfterViewInit {
  downloadSvg() {
    this.editor.getPreview().then((svgContent: any) => {
      const elem = window.document.createElement("a");
      elem.href = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svgContent);
      elem.download = "model.svg";
      document.body.appendChild(elem);
      elem.click();
      document.body.removeChild(elem);
    });
  }
  download() {
    this.editor.getContent().then((content) => {
      const elem = window.document.createElement("a");
      elem.href = "data:text/plain;charset=utf-8," + encodeURIComponent(content);
      elem.download = "model.bpmn";
      document.body.appendChild(elem);
      elem.click();
      document.body.removeChild(elem);
      // this.editor.markAsSaved();
    });
  }
  process: any
  containerId: any

  isDirty?: boolean = false

  import($event: any) {
    const file = $event.files[0]
    console.log(file)
    const reader = new FileReader();

    reader.onload = () => {
      const fileContent = reader.result as string;
      console.log('Text file content:', fileContent);
      this.editor.setContent("", fileContent)
    };

    reader.readAsText(file); // 读取为文本
  }
  editor!: EditorApi
  save() {
    const $svg = this.editor.getPreview()
    const $xml = this.editor.getContent()
    Promise.all([$svg, $xml]).then(([svg, bpmnXml]) => {
      console.log(bpmnXml)
      const body = { containerId: this.process.containerId, bpmnXml: bpmnXml, svg: svg }
      console.log(body)
      this.http.post(`${environment.bpmApi}/process-design`, body).subscribe(() => {
        this.router.navigate(['..'], { relativeTo: this.route })
      })
    })
  }

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {
    // this.containerId = this.route.snapshot.queryParams
  }

  async onBpmnLoad(): Promise<string> {
    const queryParams: any = await firstValueFrom(this.route.queryParams);
    const httpParams = new HttpParams({ fromObject: queryParams });

    let result: any;
    if (queryParams.processId) {
      result = await firstValueFrom(
        this.http.get(`${environment.bpmApi}/process-design/view`, { params: httpParams })
      );
    } else {
      result = { bpmnXml: '' };
    }

    this.process = result;
    return result.bpmnXml;
  }

  async ngAfterViewInit() {
    const editor = BpmnEditor
    console.log(editor)

    const resources: Map<string, EditorStandaloneResource> = new Map();
    resources.set("customWorkItem.wid", {
      contentType: "text",
      content: Promise.resolve(`
                  [
                    [
                      "name" : "HttpServiceTask",
                      "parameters" : [
                        "Method" : new StringDataType(),
                        "Url" : new StringDataType(),
                        "Body" : new StringDataType()
                      ],
                      "displayName" : "HTTP Service Task",
                      "icon" : "defaultservicenodeicon.png",
                      "category" : "Service"
                    ]
                  ]
                `)
    })


    this.editor = editor.open({
      container: document.getElementById('bpmn-container')!,
      initialContent: this.onBpmnLoad(), // async 返回 Promise，直接用
      readOnly: false,
      resources: resources

    });

    editor.subscribeToContentChanges((isDirty: boolean) => this.isDirty = isDirty);

  }

}

