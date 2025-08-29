import { Component, Inject, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { UntypedFormGroup, Validators, UntypedFormBuilder } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { UserService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-user-password-reset',
  templateUrl: './user-password-reset.component.html',
  styleUrls: ['./user-password-reset.component.scss']
})
export class UserPasswordResetComponent implements OnInit {
  @Input() user: any

  formGroup: UntypedFormGroup = this.fb.group({
    password: [null, Validators.required],
    confirmPassword: [null, Validators.required],
  })

  constructor(
    private fb: UntypedFormBuilder,
    private userService: UserService,
    // @Inject(MAT_DIALOG_DATA) public data:any,
    private dialog: MatDialog,
  ) {

  }

  ngOnInit(): void {
  }

  submit(){
    this.userService.resetPassword(this.formGroup.value.confirmPassword,this.user.username).subscribe()

  }

  @ViewChild('resetPassword') resetPassword!:TemplateRef<any>
  openDialog(){
    this.dialog.open(this.resetPassword)
  }

}
