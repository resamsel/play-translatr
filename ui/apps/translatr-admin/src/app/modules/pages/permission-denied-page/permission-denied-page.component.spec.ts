import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PermissionDeniedPageComponent } from './permission-denied-page.component';

describe('NotAllowedPageComponent', () => {
  let component: PermissionDeniedPageComponent;
  let fixture: ComponentFixture<PermissionDeniedPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PermissionDeniedPageComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PermissionDeniedPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
