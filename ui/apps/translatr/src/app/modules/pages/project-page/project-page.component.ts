import { Component, Inject, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, CanActivate, Route } from '@angular/router';
import { ProjectFacade } from './+state/project.facade';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { filter, take } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { canActivate$, NameIconRoute } from '@translatr/utils';
import { PROJECT_ROUTES } from './project-page.token';
import { Observable } from 'rxjs';
import { Project } from '@dev/translatr-model';

@Component({
  selector: 'app-project-page',
  templateUrl: './project-page.component.html',
  styleUrls: ['./project-page.component.scss']
})
export class ProjectPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  project$ = this.facade.project$;

  children: NameIconRoute[] = this.routes[0].children;

  form = new FormGroup({
    name: new FormControl('', [
      Validators.required,
      Validators.pattern('[^\\s/]+')
    ])
  });

  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    private readonly injector: Injector,
    private readonly route: ActivatedRoute,
    private readonly facade: ProjectFacade,
    private readonly appFacade: AppFacade,
    @Inject(PROJECT_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {
  }

  ngOnInit(): void {
    this.project$
      .pipe(
        filter(project => !!project),
        take(1)
      )
      .subscribe(project => {
        this.nameFormControl.setValue(project.name);
        this.facade.loadLocales(project.id, {});
        this.facade.loadKeys(project.id, {});
        this.facade.loadMessages(project.id, { order: 'whenCreated desc' });
        this.facade.loadActivityAggregated(project.id);
      });
  }

  ngOnDestroy(): void {
    this.facade.unloadProject();
  }

  routerLink(project: Project | undefined, route: Route): string | undefined {
    if (project === undefined) {
      return undefined;
    }

    if (route === '') {
      return `/${project.ownerUsername}/${project.name}`;
    }

    return `/${project.ownerUsername}/${project.name}/${route.path}`;
  }

  canActivate$(route: NameIconRoute): Observable<boolean> {
    return canActivate$(
      route,
      this.route,
      (guard: any) => this.injector.get<CanActivate>(guard)
    );
  }
}
