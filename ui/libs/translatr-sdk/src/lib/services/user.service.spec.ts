import { TestBed } from '@angular/core/testing';

import { UserService } from './user.service';
import { HttpClient } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';

describe('UserService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: UserService = TestBed.get(UserService);
    expect(service).toBeTruthy();
  });
});
