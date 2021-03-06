import { Injectable } from '@angular/core';
import { AccessToken, Activity, Aggregate, PagedList, Project, User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { meLoaded } from '../../../../+state/app.actions';
import { AppFacade } from '../../../../+state/app.facade';
import {
  accessTokenCreated,
  accessTokenCreateError,
  accessTokenDeleted,
  accessTokenDeleteError,
  accessTokenLoaded,
  accessTokenLoadError,
  accessTokensLoaded,
  accessTokensLoadError,
  accessTokenUpdated,
  accessTokenUpdateError,
  activitiesLoaded,
  activitiesLoadError,
  activityAggregatedLoaded,
  activityAggregatedLoadError,
  createAccessToken,
  deleteAccessToken,
  loadAccessToken,
  loadAccessTokens,
  loadActivities,
  loadActivityAggregated,
  loadProjects,
  loadUser,
  projectsLoaded,
  projectsLoadError,
  updateAccessToken,
  updateUser,
  userLoaded,
  userLoadError,
  userUpdated,
  userUpdateError
} from './user.actions';

@Injectable()
export class UserEffects {
  loadUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadUser),
      switchMap(action =>
        this.userService.byUsername(action.username).pipe(
          map((user: User) => userLoaded({ user })),
          catchError(error => of(userLoadError(error)))
        )
      )
    )
  );

  updateUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updateUser),
      switchMap(action =>
        this.userService.update(action.payload).pipe(
          withLatestFrom(this.appFacade.me$),
          switchMap(([user, me]: [User, User]) => {
            if (user.id === me.id) {
              return of(meLoaded({ payload: user }), userUpdated({ user }));
            }

            return of(userUpdated({ user }));
          }),
          catchError(error => of(userUpdateError({ error })))
        )
      )
    )
  );

  loadProjects = createEffect(() =>
    this.actions$.pipe(
      ofType(loadProjects),
      switchMap(action =>
        this.projectService
          .find({
            limit: 10,
            order: 'name asc',
            ...action
          })
          .pipe(
            map((pagedList: PagedList<Project>) => projectsLoaded({ pagedList })),
            catchError(error => of(projectsLoadError(error)))
          )
      )
    )
  );

  loadActivities = createEffect(() =>
    this.actions$.pipe(
      ofType(loadActivities),
      switchMap(action =>
        this.activityService.find(action).pipe(
          map((pagedList: PagedList<Activity>) => activitiesLoaded({ pagedList })),
          catchError(error => of(activitiesLoadError(error)))
        )
      )
    )
  );

  loadActivityAggregated$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadActivityAggregated),
      switchMap(action =>
        this.activityService.aggregated(action).pipe(
          map((pagedList: PagedList<Aggregate>) => activityAggregatedLoaded({ pagedList })),
          catchError(error => of(activityAggregatedLoadError(error)))
        )
      )
    )
  );

  loadAccessTokens = createEffect(() =>
    this.actions$.pipe(
      ofType(loadAccessTokens),
      switchMap(action =>
        this.accessTokenService.find(action).pipe(
          map((pagedList: PagedList<AccessToken>) => accessTokensLoaded({ pagedList })),
          catchError(error => of(accessTokensLoadError(error)))
        )
      )
    )
  );

  loadAccessToken = createEffect(() =>
    this.actions$.pipe(
      ofType(loadAccessToken),
      withLatestFrom(this.appFacade.me$),
      switchMap(([action, me]) => {
        if (action.id === undefined) {
          return of(
            accessTokenLoaded({
              accessToken: {
                userId: me.id,
                name: '',
                scope: ''
              }
            })
          );
        }

        return this.accessTokenService.get(action.id).pipe(
          map((accessToken: AccessToken) => accessTokenLoaded({ accessToken })),
          catchError(error => of(accessTokenLoadError(error)))
        );
      })
    )
  );

  createAccessToken = createEffect(() =>
    this.actions$.pipe(
      ofType(createAccessToken),
      switchMap(action =>
        this.accessTokenService.create(action.payload).pipe(
          map((accessToken: AccessToken) => accessTokenCreated({ payload: accessToken })),
          catchError(error => of(accessTokenCreateError(error)))
        )
      )
    )
  );

  updateAccessToken = createEffect(() =>
    this.actions$.pipe(
      ofType(updateAccessToken),
      switchMap(action =>
        this.accessTokenService.update(action.payload).pipe(
          map((accessToken: AccessToken) => accessTokenUpdated({ payload: accessToken })),
          catchError(error => of(accessTokenUpdateError(error)))
        )
      )
    )
  );

  deleteAccessToken$ = createEffect(() =>
    this.actions$.pipe(
      ofType(deleteAccessToken),
      switchMap(action =>
        this.accessTokenService.delete(action.payload.id).pipe(
          map((accessToken: AccessToken) => accessTokenDeleted({ payload: accessToken })),
          catchError(error => of(accessTokenDeleteError(error)))
        )
      )
    )
  );

  constructor(
    private readonly actions$: Actions,
    private readonly appFacade: AppFacade,
    private readonly userService: UserService,
    private readonly projectService: ProjectService,
    private readonly activityService: ActivityService,
    private readonly accessTokenService: AccessTokenService
  ) {}
}
