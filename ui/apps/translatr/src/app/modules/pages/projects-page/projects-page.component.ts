import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Params, Router } from '@angular/router';
import { Feature, ProjectCriteria, User } from '@dev/translatr-model';
import { navigate } from '@translatr/utils';
import { combineLatest } from 'rxjs';
import { distinctUntilChanged, filter, map, take, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { FilterCriteria } from '../../shared/list-header/list-header.component';
import { openProjectEditDialog } from '../../shared/project-edit-dialog/project-edit-dialog.component';
import { ProjectsFacade } from './+state/projects.facade';

@Component({
  selector: 'app-projects-page',
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  projects$ = this.facade.projects$;

  criteria$ = this.appFacade.queryParams$.pipe(
    map((params: Params) =>
      ['search', 'limit', 'offset']
        .filter(f => params[f] !== undefined && params[f] !== '')
        .reduce((acc, curr) => ({ ...acc, [curr]: params[curr] }), {})
    ),
    distinctUntilChanged(
      (a: ProjectCriteria, b: ProjectCriteria) =>
        a.search === b.search && a.limit === b.limit && a.offset === b.offset
    )
  );
  private loadProjects$ = combineLatest([this.me$.pipe(filter(user => !!user)), this.criteria$]);

  readonly Feature = Feature;

  constructor(
    private readonly facade: ProjectsFacade,
    private readonly appFacade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router
  ) {}

  ngOnInit() {
    this.loadProjects$
      .pipe(takeUntil(this.facade.unload$))
      .subscribe(([user, criteria]: [User, ProjectCriteria]) =>
        this.facade.loadProjects({
          memberId: user.id,
          order: 'whenUpdated desc',
          limit: 20,
          fetch: 'count,progress',
          ...criteria
        })
      );
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }

  openProjectCreationDialog() {
    openProjectEditDialog(this.dialog, {})
      .afterClosed()
      .pipe(
        take(1),
        filter(project => !!project)
      )
      .subscribe(project => this.router.navigate([project.ownerUsername, project.name]));
  }

  onFilter(criteria: FilterCriteria): void {
    navigate(this.router, criteria);
  }
}
