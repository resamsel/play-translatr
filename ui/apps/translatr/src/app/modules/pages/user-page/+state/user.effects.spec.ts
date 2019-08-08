import { TestBed } from '@angular/core/testing';

import { Observable } from 'rxjs';

import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { provideMockActions } from '@ngrx/effects/testing';

import { DataPersistence, NxModule } from '@nrwl/angular';
import { hot } from '@nrwl/angular/testing';

import { UserEffects } from './user.effects';
import { LoadUser, UserLoaded } from './user.actions';

describe('UserEffects', () => {
  let actions: Observable<any>;
  let effects: UserEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        NxModule.forRoot(),
        StoreModule.forRoot({}),
        EffectsModule.forRoot([])
      ],
      providers: [
        UserEffects,
        DataPersistence,
        provideMockActions(() => actions)
      ]
    });

    effects = TestBed.get(UserEffects);
  });

  describe('loadUser$', () => {
    it('should work', () => {
      actions = hot('-a-|', { a: new LoadUser() });
      expect(effects.loadUser$).toBeObservable(
        hot('-a-|', { a: new UserLoaded([]) })
      );
    });
  });
});
