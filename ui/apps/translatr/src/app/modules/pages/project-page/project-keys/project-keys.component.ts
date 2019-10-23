import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { skip, take } from 'rxjs/operators';
import { Key, KeyCriteria, Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent {
  project$ = this.facade.project$;
  keys$ = this.facade.keys$;
  criteria$ = this.facade.keysCriteria$;
  canModify$ = this.facade.canModifyKey$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar
  ) {
  }

  onMore(limit: number) {
    this.onFetch({ limit });
  }

  onFetch(criteria: KeyCriteria) {
    this.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadKeys(project.id, criteria));
  }

  onEdit(key: Key) {
    this.snackBar.open(
      `Edit key ${key.name}`,
      'Dismiss',
      { duration: 2000 }
    );
  }

  onDelete(key: Key) {
    this.facade.deleteKey(key.id);
    this.facade.keyDeleted$
      .pipe(skip(1), take(1))
      .subscribe((k) =>
        this.snackBar.open(
          `Key ${k.name} deleted`,
          'Dismiss',
          { duration: 5000 }
        )
      );
  }
}
