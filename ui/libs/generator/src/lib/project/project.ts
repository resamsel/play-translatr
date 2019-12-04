import { Injector } from '@angular/core';
import { combineLatest, Observable, of } from 'rxjs';
import { State } from '../state';
import { getRandomUserAccessToken } from '../user';
import { AccessToken, Key, Locale, Message, PagedList, Project, User, UserRole } from '@dev/translatr-model';
import { catchError, concatMap, filter, map, mapTo } from 'rxjs/operators';
import * as randomName from 'random-name';
import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage, MessageService, ProjectService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import * as _ from 'underscore';
import { createLocale, localeNames } from '../locale';
import { createKey, keyNames } from '../key';
import { getRandomProject } from './get';

const createProject = (
  project: Project,
  accessToken: AccessToken,
  projectService: ProjectService
): Observable<Project> => {
  return projectService.create(project, {
    params: { access_token: accessToken.key }
  });
};

const createMessage = (
  injector: Injector,
  message: Message,
  accessToken: AccessToken
): Observable<Message> => {
  return injector
    .get(MessageService)
    .create(message, { params: { access_token: accessToken.key } });
};

export const createRandomProject = (
  injector: Injector
): Observable<Partial<State>> => {
  // Randomly choose user, create project for that user with random name
  return getRandomUserAccessToken(
    injector,
    { limit: 10 },
    (user: User) => user.role === UserRole.User
  ).pipe(
    filter(
      (payload: { user: User; accessToken: AccessToken }) =>
        !!payload.user &&
        !!payload.user.id &&
        !!payload.accessToken &&
        !!payload.accessToken.key
    ),
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      createProject(
        {
          name: randomName.place().replace(' ', ''),
          description: 'Generated',
          ownerId: payload.user.id
        },
        payload.accessToken,
        injector.get(ProjectService)
      ).pipe(map((project: Project) => ({ ...payload, project })))
    ),
    concatMap(
      (payload: { user: User; project: Project; accessToken: AccessToken }) => {
        return combineLatest(
          _.sample(localeNames, Math.ceil(Math.random() * 12)).map(
            (localeName: string) =>
              createLocale(
                injector,
                {
                  name: localeName,
                  projectId: payload.project.id
                },
                payload.accessToken
              )
          )
        ).pipe(map((locales: Locale[]) => ({ ...payload, locales })));
      }
    ),
    concatMap(
      (payload: {
        user: User;
        project: Project;
        locales: Locale[];
        accessToken: AccessToken;
      }) => {
        return combineLatest(
          _.sample(keyNames, Math.ceil(Math.random() * 50)).map(
            (keyName: string) =>
              createKey(
                injector,
                {
                  name: keyName,
                  projectId: payload.project.id
                },
                payload.accessToken
              )
          )
        ).pipe(map((keys: Key[]) => ({ ...payload, keys })));
      }
    ),
    concatMap(
      (payload: {
        user: User;
        project: Project;
        locales: Locale[];
        keys: Key[];
        accessToken: AccessToken;
      }) => {
        const locale = pickRandomly(payload.locales);
        return combineLatest(
          payload.keys.map((key: Key) =>
            createMessage(
              injector,
              {
                localeId: locale.id,
                keyId: key.id,
                value: `${key.name} (${locale.displayName})`
              },
              payload.accessToken
            )
          )
        ).pipe(mapTo(payload));
      }
    ),
    map(
      (payload: {
        user: User;
        project: Project;
        locales: Locale[];
        keys: Key[];
      }) => ({
        message: `${payload.user.name} created project ${
          payload.project.name
        } with \
        ${payload.locales.length} languages and ${payload.keys.length} keys`
      })
    ),
    catchError((err: HttpErrorResponse) => of({ message: errorMessage(err) }))
  );
};

export const updateRandomProject = (
  injector: Injector
): Observable<Partial<State>> => {
  const projectService = injector.get(ProjectService);
  // Randomly choose user, update project of that user randomly
  return getRandomUserAccessToken(
    injector,
    { limit: 10 },
    (user: User) => user.role === UserRole.User
  ).pipe(
    filter(
      (payload: { user: User; accessToken: AccessToken }) =>
        !!payload.user &&
        !!payload.user.id &&
        !!payload.accessToken &&
        !!payload.accessToken.key
    ),
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      projectService
        .find({ owner: payload.user.id, access_token: payload.accessToken.key })
        .pipe(
          map((pagedList: PagedList<Project>) => ({
            user: payload.user,
            project: pickRandomly(pagedList.list),
            accessToken: payload.accessToken
          }))
        )
    ),
    concatMap(
      (payload: { user: User; project: Project; accessToken: AccessToken }) => {
        if (!!payload.project) {
          return of(payload);
        }

        const project: Project = {
          name: randomName.place().replace(' ', ''),
          description: 'Generated'
        };
        return createProject(project, payload.accessToken, projectService).pipe(
          map((p: Project) => ({ ...payload, project: p }))
        );
      }
    ),
    concatMap(
      (payload: { user: User; project: Project; accessToken: AccessToken }) => {
        return projectService
          .update(
            {
              ...payload.project,
              description: payload.project.description.endsWith('!')
                ? payload.project.description.replace('!', '')
                : `${payload.project.description}!`
            },
            { params: { access_token: payload.accessToken.key } }
          )
          .pipe(map((project: Project) => ({ ...payload, project })));
      }
    ),
    map((payload: { user: User; project: Project }) => ({
      message: `${payload.user.name} updated project ${payload.project.name}`
    })),
    catchError((err: HttpErrorResponse) => of({ message: errorMessage(err) }))
  );
};

export const deleteRandomProject = (
  injector: Injector
): Observable<Partial<State>> => {
  // Randomly choose user, delete project of that user randomly
  return getRandomUserAccessToken(
    injector,
    { limit: 10 },
    (user: User) => user.role === UserRole.User
  )
    .pipe(
      filter(
        (payload: { user: User; accessToken: AccessToken }) =>
          !!payload.user &&
          !!payload.user.id &&
          !!payload.accessToken &&
          !!payload.accessToken.key
      ),
      concatMap((payload: { user: User; accessToken: AccessToken }) =>
        getRandomProject(injector, payload.user, payload.accessToken).pipe(
          map((project: Project) => ({ ...payload, project }))
        )
      ),
      filter(
        (payload: { user: User; accessToken: AccessToken; project: Project }) =>
          payload.project !== undefined
      ),
      concatMap(
        (payload: { user: User; accessToken: AccessToken; project: Project }) =>
          injector
            .get(ProjectService)
            .delete(payload.project.id, {
              params: { access_token: payload.accessToken.key }
            })
            .pipe(mapTo(payload))
      )
    )
    .pipe(
      map(
        (payload: {
          user: User;
          accessToken: AccessToken;
          project: Project;
        }) => ({
          message: `${payload.user.name} deleted project ${
            payload.project.name
          }`
        })
      )
    );
};