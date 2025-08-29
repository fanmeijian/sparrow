import { HttpClient, HttpParams } from '@angular/common/http';
import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom, map, of, switchMap, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
declare global {
  interface Window {
    BpmnEditor: any;
  }
}


@Component({
  selector: 'app-process-design',
  templateUrl: './process-design.component.html',
  styleUrls: ['./process-design.component.css']
})
export class ProcessDesignComponent implements OnInit, AfterViewInit {
  process: any
  containerId: any

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
  editor: any
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
    const script = document.createElement('script');
    script.src = 'assets/bpm/index.js';

    script.onload = async () => {
      const editor = (window as any).BpmnEditor;
      if (editor) {
        this.editor = editor.open({
          container: document.getElementById('bpmn-container')!,
          initialContent: this.onBpmnLoad(), // async 返回 Promise，直接用
          fileName: 'diagram.bpmn',
          readOnly: false,
        });
      }
    };

    document.body.appendChild(script);
  }

}
