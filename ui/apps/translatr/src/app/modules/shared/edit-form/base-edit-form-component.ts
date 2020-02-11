import { Observable } from 'rxjs';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { filter, take } from 'rxjs/operators';
import { ConstraintViolation, Error } from '@dev/translatr-model';
import { EventEmitter, HostListener, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';

const ENTER_KEYCODE = 'Enter';

export interface Identifiable {
  id?: number | string;
}

export abstract class BaseEditFormComponent<T, F extends Identifiable, R extends Identifiable = F> {
  processing = false;
  log = console.log;

  @Output() save = new EventEmitter<R>();
  @Output() failure = new EventEmitter<Error>();

  constructor(
    protected readonly snackBar: MatSnackBar,
    protected readonly dialogRef: MatDialogRef<T, R>,
    readonly form: FormGroup,
    protected readonly data: Partial<F>,
    protected readonly create: (r: F) => void,
    protected readonly update: (r: F) => void,
    protected readonly result$: Observable<R>,
    protected readonly messageProvider: (r: R) => string
  ) {
    this.form.patchValue(data);
  }

  get invalid(): boolean {
    return this.form.invalid;
  }

  public onSave(): void {
    this.processing = true;

    const value: F = this.form.value;
    this.result$
      .pipe(filter(x => !!x), take(1))
      .subscribe(
        (r: R) => this.onSuccess(r),
        (res: { error: Error }) => this.onError(res.error)
      );

    if (value.id) {
      this.update(value);
    } else {
      this.create(value);
    }
  }

  protected isValid(): boolean {
    return this.form.valid;
  }

  protected onSuccess(r: R): void {
    this.processing = false;
    this.onSaved(r);

    this.snackBar.open(
      this.messageProvider(r),
      'Dismiss',
      { duration: 3000 }
    );
  }

  protected onSaved(r: R): void {
    if (this.dialogRef !== undefined) {
      this.dialogRef.close(r);
    }
  }

  protected onError(error: Error | undefined): void {
    this.processing = false;
    if (error && error.error.violations) {
      error.error.violations.forEach((violation: ConstraintViolation) => {
        const control = this.form.get(violation.field);
        if (!!control) {
          control.setErrors({ violation: violation.message });
          control.markAsTouched();
        }
      });
    }
    this.failure.emit(error);
  }

  @HostListener('window:keyup', ['$event'])
  protected onHotkey(event: KeyboardEvent) {
    if (event.key === ENTER_KEYCODE
      && event.ctrlKey === true
      && this.isValid() && !this.processing) {
      this.onSave();
    }
  }
}