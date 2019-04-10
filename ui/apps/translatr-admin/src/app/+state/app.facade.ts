import {Injectable} from '@angular/core';
import {select, Store} from '@ngrx/store';
import {AppPartialState} from './app.reducer';
import {appQuery} from './app.selectors';
import {AppAction, AppActionTypes, DeleteUser, LoadLoggedInUser, LoadUsers} from './app.actions';
import {User} from "@dev/translatr-sdk";
import {Actions, ofType} from "@ngrx/effects";

@Injectable()
export class AppFacade {
  me$ = this.store.pipe(select(appQuery.getLoggedInUser));
  users$ = this.store.pipe(select(appQuery.getUsers));
  userDeleted$ = this.actions$.pipe(
    ofType(AppActionTypes.UserDeleted, AppActionTypes.UserDeleteError)
  );

  constructor(
    private readonly store: Store<AppPartialState>,
    private readonly actions$: Actions) {
  }

  loadLoggedInUser() {
    this.store.dispatch(new LoadLoggedInUser());
  }

  loadUsers() {
    this.store.dispatch(new LoadUsers());
  }

  deleteUser(user: User) {
    this.store.dispatch(new DeleteUser(user));
  }
}
