import { Component } from '@angular/core';
import { merge, Observable, of } from "rxjs";
import { AccessToken, RequestCriteria } from "@dev/translatr-model";
import { AppFacade } from "../../../../+state/app.facade";
import { Entity } from "@dev/translatr-components";
import {
  hasDeleteAccessTokenPermission,
  hasDeleteAllAccessTokensPermission,
  hasEditAccessTokenPermission
} from "@dev/translatr-sdk/src/lib/shared/permissions";
import {
  AccessTokenDeleted,
  AccessTokenDeleteError,
  AccessTokensDeleted,
  AccessTokensDeleteError,
  AppActionTypes
} from "../../../../+state/app.actions";
import { errorMessage } from "@dev/translatr-sdk";
import { MatDialog, MatSnackBar } from "@angular/material";
import { mapTo } from "rxjs/operators";
import { ofType } from "@ngrx/effects";
import { notifyEvent } from "@dev/translatr-components/src/lib/modules/events/utils";

@Component({
  selector: 'dev-dashboard-access-tokens',
  templateUrl: './dashboard-access-tokens.component.html',
  styleUrls: ['./dashboard-access-tokens.component.css']
})
export class DashboardAccessTokensComponent {

  displayedColumns = ['name', 'user', 'scopes', 'when_created', 'actions'];

  me$ = this.facade.me$;
  accessTokens$ = this.facade.accessTokens$;
  load$ = merge(
    of({limit: '20', order: 'name asc'}),
    this.facade.accessTokenDeleted$.pipe(ofType(AppActionTypes.AccessTokenDeleted), mapTo({})),
    this.facade.accessTokensDeleted$.pipe(ofType(AppActionTypes.AccessTokensDeleted), mapTo({}))
  );

  selected: AccessToken[] = [];

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    notifyEvent(
      snackBar,
      facade.accessTokenDeleted$,
      AppActionTypes.AccessTokenDeleted,
      (action: AccessTokenDeleted) => `Access token ${action.payload.name} has been deleted`,
      (action: AccessTokenDeleteError) => `Access token could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.accessTokensDeleted$,
      AppActionTypes.AccessTokensDeleted,
      (action: AccessTokensDeleted) => `${action.payload.length} access tokens have been deleted`,
      (action: AccessTokensDeleteError) => `Access tokens could not be deleted: ${errorMessage(action.payload)}`
    );
  }

  onSelected(entities: Entity[]) {
    this.selected = entities as AccessToken[];
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadAccessTokens(criteria);
  }

  allowEdit$(accessToken: AccessToken): Observable<boolean> {
    return this.me$.pipe(hasEditAccessTokenPermission(accessToken));
  }

  onEdit(accessToken: AccessToken) {
    // this.facade.deleteAccessToken(accessToken);
  }

  allowDelete$(accessToken: AccessToken): Observable<boolean> {
    return this.me$.pipe(hasDeleteAccessTokenPermission(accessToken));
  }

  onDelete(accessToken: AccessToken) {
    this.facade.deleteAccessToken(accessToken);
  }

  allowDeleteAll$(accessTokens: AccessToken[]): Observable<boolean> {
    return this.me$.pipe(hasDeleteAllAccessTokensPermission(accessTokens));
  }

  onDeleteAll(accessTokens: AccessToken[]) {
    this.facade.deleteAccessTokens(accessTokens);
  }
}