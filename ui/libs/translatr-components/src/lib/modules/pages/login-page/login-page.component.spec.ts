import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterTestingModule } from '@angular/router/testing';
import { FontAwesomeTestingModule } from '@fortawesome/angular-fontawesome/testing';
import { NavbarTestingModule } from '@translatr/components/testing';
import { ENDPOINT_URL } from '@translatr/utils';
import { mockObservable } from '@translatr/utils/testing';

import { LoginPageComponent } from './login-page.component';
import { AuthClientService } from '@dev/translatr-sdk';

describe('LoginPageComponent', () => {
  let component: LoginPageComponent;
  let fixture: ComponentFixture<LoginPageComponent>;
  let authProviderService: AuthClientService & { find: jest.Mock };

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [LoginPageComponent],
        imports: [
          RouterTestingModule,

          NavbarTestingModule,

          MatIconModule,
          MatCardModule,
          FontAwesomeTestingModule
        ],
        providers: [
          {
            provide: AuthClientService,
            useFactory: () => ({
              find: jest.fn()
            })
          },
          { provide: ENDPOINT_URL, useValue: '' }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    authProviderService = TestBed.get(AuthClientService);
    authProviderService.find.mockReturnValue(mockObservable());

    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
