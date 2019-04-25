import { Component, OnDestroy } from '@angular/core';
import { AppFacade } from "../../../../+state/app.facade";
import { Project, RequestCriteria } from "@dev/translatr-model";
import { Observable, of } from "rxjs";
import {
  AppActionTypes,
  ProjectDeleted,
  ProjectDeleteError,
  ProjectsDeleted,
  ProjectsDeleteError
} from "../../../../+state/app.actions";
import { MatDialog, MatSnackBar } from "@angular/material";
import { errorMessage } from "@dev/translatr-sdk";
import {
  hasDeleteAllProjectsPermission,
  hasDeleteProjectPermission,
  hasEditProjectPermission
} from "@dev/translatr-sdk/src/lib/shared/permissions";
import { Entity } from "@dev/translatr-components";
import { notifyEvent } from "@dev/translatr-components/src/lib/modules/events/utils";
import { ProjectEditDialogComponent } from "@dev/translatr-components/src/lib/project/project-edit-dialog/project-edit-dialog.component";

@Component({
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.css']
})
export class DashboardProjectsComponent implements OnDestroy {

  displayedColumns = ['name', 'description', 'owner', 'when_created', 'actions'];

  me$ = this.facade.me$;
  projects$ = this.facade.projects$;
  load$ = of({limit: '20', order: 'name asc'});

  selected: Project[] = [];

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    notifyEvent(
      snackBar,
      facade.projectDeleted$,
      AppActionTypes.ProjectDeleted,
      (action: ProjectDeleted) => `Project ${action.payload.name} has been deleted`,
      (action: ProjectDeleteError) => `Project could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.projectsDeleted$,
      AppActionTypes.ProjectsDeleted,
      (action: ProjectsDeleted) => `${action.payload.length} projects have been deleted`,
      (action: ProjectsDeleteError) => `Projects could not be deleted: ${errorMessage(action.payload)}`
    );
  }

  onSelected(entities: Entity[]) {
    this.selected = entities as Project[];
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadProjects(criteria);
  }

  allowEdit$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasEditProjectPermission(project));
  }

  onEdit(project: Project) {
    this.dialog
      .open(ProjectEditDialogComponent, {
        data: {
          type: 'update',
          project,
          onSubmit: (project: Project) => this.facade.updateProject(project),
          success$: this.facade.projectUpdated$,
          error$: this.facade.projectUpdateError$
        }
      });
  }

  allowDelete$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasDeleteProjectPermission(project));
  }

  onDelete(project: Project) {
    this.facade.deleteProject(project);
  }

  allowDeleteAll$(projects: Project[]): Observable<boolean> {
    return this.me$.pipe(hasDeleteAllProjectsPermission(projects));
  }

  onDeleteAll(projects: Project[]) {
    this.facade.deleteProjects(projects);
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }
}
