import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityAccessTokenLinkComponent } from './activity-access-token-link.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('ActivityAccessTokenLinkComponent', () => {
  let component: ActivityAccessTokenLinkComponent;
  let fixture: ComponentFixture<ActivityAccessTokenLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityAccessTokenLinkComponent],
      imports: [RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityAccessTokenLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});