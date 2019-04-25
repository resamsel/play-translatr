import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material";
import { ConstraintViolation, ConstraintViolationErrorInfo, ErrorAction, User, UserRole } from "@dev/translatr-model";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Observable } from "rxjs";
import { takeUntil } from "rxjs/operators";

export interface UserEditDialogConfig {
  type: 'create' | 'update';
  allowedRoles: UserRole[];
  user?: User;
  onSubmit: (user: User) => void;
  success$: Observable<User>;
  error$: Observable<ErrorAction>;
}

const defaultUser: Partial<User> = {
  role: UserRole.User
};

@Component({
  selector: 'dev-user-edit-dialog',
  templateUrl: './user-edit-dialog.component.html',
  styleUrls: ['./user-edit-dialog.component.css']
})
export class UserEditDialogComponent {
  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl('', Validators.required),
    role: new FormControl('', Validators.required),
    username: new FormControl('', Validators.required),
    email: new FormControl()
  });
  roles: UserRole[] = [UserRole.Admin, UserRole.User];

  constructor(
    public dialogRef: MatDialogRef<UserEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserEditDialogConfig
  ) {
    this.form.patchValue(data.user !== undefined ? data.user : {...defaultUser});
    data.success$
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe(() => dialogRef.close());
    data.error$
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe((action: ErrorAction) => this.setErrors(action.payload.error.error));
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSubmit() {
    this.data.onSubmit(this.form.value);
  }

  private setErrors(error: ConstraintViolationErrorInfo) {
    console.log(error);
    if (error.type === 'ConstraintViolationException') {
      error.violations
        .filter((violation: ConstraintViolation) => !!this.form.get(violation.field))
        .forEach((violation: ConstraintViolation) =>
          this.form.get(violation.field).setErrors({'violation': violation.message}));
    } else {
      this.form.setErrors({'': error.message});
    }
  }
}