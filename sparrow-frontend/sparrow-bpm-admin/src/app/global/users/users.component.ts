import { User } from './../../../lib/model/user';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { Component, inject } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent {
  addGroups(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our keyword
    if (value) {
      this.selections.groups.push(value);
    }

    // Clear the input value
    event.chipInput!.clear();
  }
  removeGroups(keyword: string) {
    const index = this.keywords.indexOf(keyword);
    if (index >= 0) {
      this.selections.groups.splice(index, 1);

      this.announcer.announce(`removed ${keyword}`);
    }
  }
  selections: any = { users: [], groups: [] }
  keywords = ['angular', 'how-to', 'tutorial', 'accessibility'];
  formControl = new FormControl(['angular']);

  announcer = inject(LiveAnnouncer);

  removeKeyword(keyword: string) {
    const index = this.keywords.indexOf(keyword);
    if (index >= 0) {
      this.selections.users.splice(index, 1);

      this.announcer.announce(`removed ${keyword}`);
    }
  }

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our keyword
    if (value) {
      this.selections.users.push(value);
    }

    // Clear the input value
    event.chipInput!.clear();
  }
}
