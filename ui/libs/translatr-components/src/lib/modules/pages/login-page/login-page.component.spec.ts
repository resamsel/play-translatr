import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginPageComponent } from './login-page.component';
import { MatIconModule } from '@angular/material';
import { NavbarTestingModule } from '@translatr/components/testing';
import { AuthProviderService } from '@translatr/translatr-sdk/src/lib/services/auth-provider.service';
import { ENDPOINT_URL } from '@translatr/utils';
import { mockObservable } from '@translatr/utils/testing';

describe('LoginPageComponent', () => {
  let component: LoginPageComponent;
  let fixture: ComponentFixture<LoginPageComponent>;
  let authProviderService: AuthProviderService & { find: jest.Mock };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LoginPageComponent],
      imports: [
        NavbarTestingModule,

        MatIconModule
      ],
      providers: [
        {
          provide: AuthProviderService,
          useFactory: () => ({
            find: jest.fn()
          })
        },
        { provide: ENDPOINT_URL, useValue: '' }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    authProviderService = TestBed.get(AuthProviderService);
    authProviderService.find.mockReturnValue(mockObservable());

    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
