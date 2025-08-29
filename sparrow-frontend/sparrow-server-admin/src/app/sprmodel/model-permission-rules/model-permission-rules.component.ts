import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
} from "@angular/core";
import { MatLegacyTableDataSource as MatTableDataSource } from "@angular/material/legacy-table";
// import { MonacoEditorService } from "../../../service/monaco-editor.service";
import { first } from "rxjs";
// import * as monaco from "monaco-editor";

@Component({
  selector: "app-model-permission-rules",
  templateUrl: "./model-permission-rules.component.html",
  styleUrls: ["./model-permission-rules.component.scss"],
})
export class ModelPermissionRulesComponent implements OnInit, AfterViewInit {
  @Input() data: any[] = [];

  dataSource = new MatTableDataSource<any>();
  pageable = { pageIndex: 0, pageSize: 10, length: 0 };

  displayedColumns = ["id", "name", "code", "users"];

  public _editor: any;
  @ViewChild("editorContainer", { static: true })
  _editorContainer!: ElementRef<any>;

  constructor(
    // private monacoEditorService: MonacoEditorService
  ) {}
  ngAfterViewInit(): void {

    // this.initMonaco();
  }



  ngOnInit(): void {
    // this.monacoEditorService.load();
    // this.monacoEditorService.initMonaco( this._editorContainer.nativeElement).then(res=>{
    //   this._editor=res
    // })

  }

  onPageChange(event: any) {
    this.dataSource = new MatTableDataSource<any>(this.data);
  }

  new() {
    this.data.push({});
    this.dataSource = new MatTableDataSource<any>(this.data);
    // console.log(this._editor.getValue())
  }

  remove(a: any, b: any) {}

  edit(a: any) {}
  delete(i: number) {
    this.data.splice(i, 1);
    this.dataSource = new MatTableDataSource<any>(this.data);
  }
}
