import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { Actions } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { ProjectEffects } from './project.effects';
import { ActivityService, KeyService, LocaleService, MessageService } from '@dev/translatr-sdk';
import { Locale, PagedList } from '@dev/translatr-model';
import { loadLocales, localesLoaded } from './project.actions';
import { MemberService } from '@translatr/translatr-sdk/src/lib/services/member.service';
import { ProjectState } from './project.reducer';

describe('ProjectEffects', () => {
  let actions: Subject<any>;
  let store: Store<ProjectState> & {
    dispatch: jest.Mock;
    pipe: jest.Mock;
    select: jest.Mock;
  };
  let localeService: LocaleService & {
    find: jest.Mock;
  };

  beforeEach(() => {
    actions = new BehaviorSubject(undefined);
    TestBed.configureTestingModule({
      providers: [
        ProjectEffects,
        {
          provide: LocaleService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        {
          provide: KeyService,
          useFactory: () => ({})
        },
        {
          provide: MemberService,
          useFactory: () => ({})
        },
        {
          provide: MessageService,
          useFactory: () => ({})
        },
        {
          provide: ActivityService,
          useFactory: () => ({})
        },
        { provide: Actions, useValue: actions },
        {
          provide: Store, useFactory: () => ({
            dispatch: jest.fn(),
            pipe: jest.fn(),
            select: jest.fn()
          })
        }
      ]
    });

    store = TestBed.get(Store);
    store.pipe.mockReturnValue(of({}));
    localeService = TestBed.get(LocaleService);
  });

  describe('loadLocales$', () => {
    it('should work', (done) => {
      // given
      const payload: PagedList<Locale> = {
        list: [],
        hasNext: false,
        hasPrev: false,
        limit: 20,
        offset: 0
      };
      const criteria = {};
      localeService.find.mockReturnValueOnce(of(payload));
      const effects = TestBed.get(ProjectEffects);
      const target$ = effects.loadLocales$;

      // when
      actions.next(loadLocales({ payload: criteria }));

      // then
      target$.subscribe(actual => {
        expect(actual).toStrictEqual(localesLoaded({ payload }));
        expect(localeService.find.mock.calls.length).toBe(1);
        done();
      });
    });
  });
});
