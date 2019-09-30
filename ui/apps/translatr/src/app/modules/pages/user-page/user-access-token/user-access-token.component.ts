import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserFacade } from '../+state/user.facade';
import { filter, take } from 'rxjs/operators';
import { AccessToken } from '@dev/translatr-model';

const distinct = <T>(value: T, index: number, self: Array<T>) =>
  self.indexOf(value) === index;

const scopeType = scope => scope.split(':')[1];
const scopePermission = scope => scope.split(':')[0];

@Component({
  selector: 'app-user-access-token',
  templateUrl: './user-access-token.component.html',
  styleUrls: ['./user-access-token.component.scss']
})
export class UserAccessTokenComponent implements OnInit {
  accessToken$ = this.facade.accessToken$
    .pipe(filter(x => !!x));

  constructor(
    private readonly route: ActivatedRoute,
    private readonly facade: UserFacade
  ) {
  }

  ngOnInit() {
    this.route.params
      .pipe(take(1))
      .subscribe(params => this.facade.loadAccessToken(params.id));
  }

  onSaved(accessToken: AccessToken) {
  }
}
