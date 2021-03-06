import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { AuthBarLanguageSwitcherComponent } from './auth-bar-language-switcher.component';

describe('AuthBarLanguageSwitcherComponent', () => {
  let component: AuthBarLanguageSwitcherComponent;
  let fixture: ComponentFixture<AuthBarLanguageSwitcherComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [AuthBarLanguageSwitcherComponent],
        imports: [TranslocoTestingModule, MatMenuModule, MatIconModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthBarLanguageSwitcherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
