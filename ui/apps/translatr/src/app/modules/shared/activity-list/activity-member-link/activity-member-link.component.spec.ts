import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslocoTestingModule } from '@ngneat/transloco';

import { ActivityMemberLinkComponent } from './activity-member-link.component';

describe('ActivityMemberLinkComponent', () => {
  let component: ActivityMemberLinkComponent;
  let fixture: ComponentFixture<ActivityMemberLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ActivityMemberLinkComponent],
      imports: [RouterTestingModule, TranslocoTestingModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActivityMemberLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
