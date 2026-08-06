import { TestBed } from '@angular/core/testing';

import { Renta } from './renta';

describe('Renta', () => {
  let service: Renta;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Renta);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
