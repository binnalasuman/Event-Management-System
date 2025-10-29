import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventsService } from '../../core/events.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-book-event',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './book-event.html',
  styleUrls: ['./book-event.scss']
})
export class BookEventComponent {
  eventId: number;
  seats: number = 1;
  passes?: string[];
  error?: string; // <-- add this line

  constructor(private route: ActivatedRoute, private svc: EventsService) {
    this.eventId = Number(this.route.snapshot.paramMap.get('eventId'));
  }

  submitBooking() {
    this.svc.book(this.eventId, this.seats).subscribe({
      next: (result) => {
        this.passes = result.passes;
        this.error = undefined;
      },
      error: (err) => {
        this.error = 'Booking failed: ' + (err.error?.message || 'Unknown error');
      }
    });
  }
}
