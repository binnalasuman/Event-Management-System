import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventBookings } from './event-bookings';

describe('EventBookings', () => {
  let component: EventBookings;
  let fixture: ComponentFixture<EventBookings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventBookings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventBookings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
