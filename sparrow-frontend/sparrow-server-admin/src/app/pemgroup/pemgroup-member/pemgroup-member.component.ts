import { ENTER, COMMA } from '@angular/cdk/keycodes';
import { Component, Inject, OnInit } from '@angular/core';
import { MatLegacyChipInputEvent as MatChipInputEvent } from '@angular/material/legacy-chips';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { PemgroupService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-pemgroup-member',
  templateUrl: './pemgroup-member.component.html',
  styleUrls: ['./pemgroup-member.component.scss']
})
export class PemgroupMemberComponent implements OnInit {
  selectedSysroles: any[] = [];
  selectedUsernames: any[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private sysroleService: PemgroupService,
  ) {}

  ngOnInit(): void {
    console.log(this.data);
  }

  submit() {
    this.sysroleService.addGroupMembers({
      sysroleIds:this.selectedSysroles.map(m=>Object.assign({},{groupId: this.data.id,sysroleId:m.id})),
      usernames: this.selectedUsernames.map(m=>Object.assign({},{username: m.username, groupId:this.data.id}))
    }).subscribe()

  }


  addOnBlur = true;
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  fruits: any[] = [];

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our fruit
    if (value) {
      this.fruits.push({name: value});
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  remove(fruit: any): void {
    const index = this.fruits.indexOf(fruit);

    if (index >= 0) {
      this.fruits.splice(index, 1);
    }
  }

}
