import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@angular/cdk/layout';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SidenavModule } from './modules/nav/sidenav/sidenav.module';
import { NxModule } from '@nrwl/angular';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { NotificationService, TranslatrSdkModule } from '@dev/translatr-sdk';
import { appReducer, initialState as appInitialState } from './+state/app.reducer';
import { AppEffects } from './+state/app.effects';
import { AppFacade } from './+state/app.facade';
import { environment } from '../environments/environment';
import { ENDPOINT_URL, LOGIN_URL, WINDOW } from '@translatr/utils';
import { MatSnackBar, MatSnackBarModule } from '@angular/material';
import { MatNotificationService } from './services/mat-notification-service';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,

    AppRoutingModule,
    TranslatrSdkModule,
    SidenavModule,
    LayoutModule,

    MatToolbarModule,
    MatButtonModule,
    MatSnackBarModule,

    NxModule.forRoot(),
    StoreModule.forRoot(
      { app: appReducer },
      {
        initialState: { app: appInitialState },
        metaReducers: [],
        runtimeChecks: {
          strictStateImmutability: true,
          strictActionImmutability: true
        }
      }
    ),
    EffectsModule.forRoot([AppEffects]),
    !environment.production ? StoreDevtoolsModule.instrument() : []
  ],
  providers: [
    AppFacade,
    { provide: WINDOW, useFactory: () => window },
    { provide: ENDPOINT_URL, useValue: environment.endpointUrl },
    { provide: LOGIN_URL, useValue: `${environment.endpointUrl}/login` },
    {
      provide: NotificationService,
      useFactory: (snackBar: MatSnackBar) => new MatNotificationService(snackBar),
      deps: [MatSnackBar]
    }

  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
