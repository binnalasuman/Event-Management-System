import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventsService } from '../../core/events.service';
import { CommonModule } from '@angular/common';

interface Booking {
  user: string; // or email
  pass: string;
}

@Component({
  selector: 'app-event-bookings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './event-bookings.html',
  styleUrls: ['./event-bookings.scss']
})
export class EventBookingsComponent implements OnInit {
  bookings: Booking[] = [];
  loading = false;
  error?: string;
  eventId?: number;

  constructor(private route: ActivatedRoute, private svc: EventsService) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id')); // Adjust param name if needed
    this.fetchBookings();
  }

  fetchBookings() {
    if (!this.eventId) return;
    this.loading = true;
    this.error = undefined;
    this.svc.getEventBookings(this.eventId).subscribe({
      next: (bookings) => {
        this.bookings = bookings; // NOT bookings.bookings!
        this.loading = false;
        this.error = undefined;
      },
      error: () => {
        this.error = 'Failed to load bookings';
        this.loading = false;
      }
    });
  }

  copyToClipboard(pass: string): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(pass)
        .catch(() => alert('Copy failed!'));
    } else {
      // Fallback (rarely required)
      const input = document.createElement('input');
      input.style.opacity = '0';
      input.value = pass;
      document.body.appendChild(input);
      input.select();
      try { document.execCommand('copy'); } catch { alert('Copy failed!'); }
      document.body.removeChild(input);
    }
  }
}
