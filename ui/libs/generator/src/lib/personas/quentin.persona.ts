import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, concatMap, map } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomLocaleForProject } from '../locale';
import { updateMessage } from '../message';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'translation',
  type: 'create',
  name: 'Quentin',
  description:
    "I'm going to translate all missing keys for a language in a random project of mine.",
  weight: 30
};

export class QuentinPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService,
      this.keyService,
      this.messageService
    ).pipe(
      concatMap(({ accessToken, project }) =>
        selectRandomLocaleForProject(
          this.localeService,
          project,
          accessToken,
          this.config.accessToken
        ).pipe(map(locale => ({ accessToken, project, locale })))
      ),
      concatMap(({ accessToken, project, locale }) =>
        this.keyService
          .find({
            projectId: project.id,
            localeId: locale.id,
            limit: 200,
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.ProjectRead,
              Scope.KeyRead
            )
          })
          .pipe(map(paged => ({ accessToken, project, locale, keys: paged.list })))
      ),
      concatMap(({ accessToken, project, locale, keys }) =>
        this.messageService
          .find({
            projectId: project.id,
            localeId: locale.id,
            limit: 200,
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.ProjectRead,
              Scope.MessageRead
            )
          })
          .pipe(map(paged => ({ accessToken, project, locale, keys, messages: paged.list })))
      ),
      concatMap(({ accessToken, project, locale, keys, messages }) => {
        const existingKeyIds = messages.map(message => message.keyId);
        return combineLatest(
          // Only create message for missing translations
          keys
            .filter(key => !existingKeyIds.includes(key.id))
            .map(key =>
              updateMessage(
                this.messageService,
                project,
                locale,
                key,
                accessToken,
                this.config.accessToken
              )
            )
        ).pipe(map(() => ({ project, locale, keys })));
      }),
      map(
        ({ project, locale, keys }) =>
          `${keys.length} translations for language ${locale.name} for project ${project.ownerUsername}/${project.name} created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new QuentinPersona(config, injector)
});
