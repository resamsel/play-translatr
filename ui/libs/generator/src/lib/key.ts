import { Injector } from '@angular/core';
import { AccessToken, Key } from '@dev/translatr-model';
import { KeyService } from '@dev/translatr-sdk';
import { cartesianProduct } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { State } from './state';

export const featureNames = [
  'user',
  'users',
  'project',
  'projects',
  'locale',
  'locales',
  'key',
  'keys',
  'message',
  'messages',
  'accessToken',
  'accessTokens',
  'member',
  'members',
  'activity',
  'activities',
  'dashboard',
  'admin'
];
export const parts = ['list', 'detail', 'find', 'search', 'main', 'header', 'footer'];
export const keySuffixes = [
  'title',
  'description',
  'text',
  'comment',
  'get',
  'create',
  'update',
  'delete',
  'permission',
  'sell',
  'confirm',
  'allow',
  'restricted',
  'filter',
  'clear'
];
export const keyNames = cartesianProduct([
  featureNames,
  parts,
  keySuffixes
]).map((values: string[]) => values.join('.'));

export const createKey = (
  injector: Injector,
  key: Key,
  accessToken: AccessToken
): Observable<Key> => {
  return injector.get(KeyService).create(key, { params: { access_token: accessToken.key } });
};

export const createRandomKey = (injector: Injector): Observable<Partial<State>> => {
  return of({ message: '+++ Create Random Key +++' });
};

export const deleteRandomKey = (injector: Injector): Observable<Partial<State>> => {
  return of({ message: '+++ Delete Random Key +++' });
};
