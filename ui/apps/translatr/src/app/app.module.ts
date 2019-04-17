import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MainPageModule } from './modules/pages/main-page/main-page.module';
import { ProjectPageModule } from './modules/pages/project-page/project-page.module';
import { LayoutModule } from '@angular/cdk/layout';
import { MatButtonModule, MatToolbarModule } from '@angular/material';
import { SidenavModule } from './modules/nav/sidenav/sidenav.module';
import { UserPageModule } from './modules/pages/user-page/user-page.module';
import { NxModule } from '@nrwl/nx';
import { ProjectsPageModule } from './modules/pages/projects-page/projects-page.module';
import { UsersPageModule } from './modules/pages/users-page/users-page.module';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { EditorPageModule } from './modules/pages/editor-page/editor-page.module';
import { TranslatrSdkModule } from '@dev/translatr-sdk';
import {
  APP_FEATURE_KEY,
  initialState as appInitialState,
  appReducer
} from './+state/app.reducer';
import { AppEffects } from './+state/app.effects';
import { AppFacade } from './+state/app.facade';
import { environment } from '../environments/environment';
import { storeFreeze } from 'ngrx-store-freeze';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    TranslatrSdkModule,
    SidenavModule,
    MainPageModule,
    // DashboardPageModule,
    ProjectsPageModule,
    UsersPageModule,
    UserPageModule,
    ProjectPageModule,
    EditorPageModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule,

    NxModule.forRoot(),
    StoreModule.forRoot(
      { app: appReducer },
      {
        initialState: { app: appInitialState },
        metaReducers: !environment.production ? [storeFreeze] : []
      }
    ),
    EffectsModule.forRoot([AppEffects]),
    !environment.production ? StoreDevtoolsModule.instrument() : []
  ],
  providers: [AppFacade],
  bootstrap: [AppComponent]
})
export class AppModule {}
