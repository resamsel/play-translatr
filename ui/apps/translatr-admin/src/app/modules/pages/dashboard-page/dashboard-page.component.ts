import { Component, Inject, OnInit } from '@angular/core';
import { AppFacade } from '../../../+state/app.facade';
import { Route, Router } from '@angular/router';
import { DASHBOARD_ROUTES } from './dashboard-page.token';
import { NameIconRoute } from '@translatr/utils';

@Component({
  selector: 'dev-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss']
})
export class DashboardPageComponent implements OnInit {
  me$ = this.facade.me$;
  children: NameIconRoute[] = this.routes[0].children;

  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    @Inject(DASHBOARD_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {}

  ngOnInit() {}

  routerLink(route: Route) {
    if (route === '') {
      return '/';
    }

    return `/${route.path}`;
  }

  isLinkActive(url) {
    let charPos = this.router.url.indexOf('?');
    let cleanUrl = charPos !== -1 ? this.router.url.slice(0, charPos) : this.router.url;
    return (cleanUrl === url);
  }
}
