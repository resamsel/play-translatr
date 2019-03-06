import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {UserPageRoutingModule} from './user-page-routing.module';
import {UserPageComponent} from './user-page.component';
import {UserProjectsComponent} from './user-projects/user-projects.component';
import {
  MatButtonModule,
  MatCardModule,
  MatChipsModule,
  MatDialogModule,
  MatIconModule,
  MatTabsModule
} from "@angular/material";
import {RouterModule} from "@angular/router";
import {SidenavModule} from "../../nav/sidenav/sidenav.module";
import {MomentModule} from "ngx-moment";
import {ProjectListModule} from "../../shared/project-list/project-list.module";
import {GravatarModule} from "ngx-gravatar";
import {UserInfoComponent} from './user-info/user-info.component';
import {ActivityModule} from "../../shared/activity/activity.module";
import {ActivityListModule} from "../../shared/activity-list/activity-list.module";
import {UserActivityComponent} from './user-activity/user-activity.component';

@NgModule({
  declarations: [UserPageComponent, UserProjectsComponent, UserInfoComponent, UserActivityComponent],
  imports: [
    CommonModule,
    RouterModule,
    UserPageRoutingModule,
    SidenavModule,
    ProjectListModule,
    ActivityListModule,
    ActivityModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    MatCardModule,
    MatButtonModule,
    MomentModule,
    GravatarModule,
    MatDialogModule
  ]
})
export class UserPageModule {
}
