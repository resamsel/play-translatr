import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectCriteria, User } from '@dev/translatr-model';
import { openProjectEditDialog } from '../../../shared/project-edit-dialog/project-edit-dialog.component';
import { filter, take, takeUntil } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { UserFacade } from '../+state/user.facade';
import { BehaviorSubject, Subject } from 'rxjs';

@Component({
  selector: 'app-user-projects',
  templateUrl: './user-projects.component.html',
  styleUrls: ['./user-projects.component.scss']
})
export class UserProjectsComponent implements OnInit {
  projects$ = this.facade.projects$;
  criteria$: Subject<ProjectCriteria> =
    new BehaviorSubject<ProjectCriteria>({
      order: 'whenUpdated desc',
      limit: 10
    });

  constructor(
    private readonly facade: UserFacade,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.facade.user$
      .pipe(filter(user => !!user))
      .subscribe((user: User) => {
        this.updateCriteria({ owner: user.username });

        this.criteria$.pipe(takeUntil(this.facade.destroy$))
          .subscribe((criteria) => this.facade.loadProjects(criteria));
      });
  }

  onFilter(search: string) {
    this.updateCriteria({ search });
  }

  onMore(limit: number) {
    this.updateCriteria({ limit });
  }

  updateCriteria(updatedCriteria: Partial<ProjectCriteria>): void {
    this.criteria$
      .asObservable()
      .pipe(take(1))
      .subscribe((criteria) => {
        if (!!updatedCriteria.search) {
          criteria.search = updatedCriteria.search;
        } else {
          delete criteria.search;
        }

        this.criteria$.next({ ...criteria, ...updatedCriteria });
      });
  }

  openProjectCreationDialog(): void {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe((project => this.router
        .navigate([project.ownerUsername, project.name])));
  }
}
