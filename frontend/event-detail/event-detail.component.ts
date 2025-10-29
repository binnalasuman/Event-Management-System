import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { EventsService } from '../../core/events.service';
import { Event } from '../../core/event';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './event-detail.html',
  styleUrls: ['./event-detail.scss']
})
export class EventDetailComponent {
  event?: Event;
  loading = false;
  error?: string;

  constructor(private route: ActivatedRoute, private router: Router, private svc: EventsService) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.error = 'Invalid event id'; return; }
    this.loading = true;
    this.svc.get(id).subscribe({
      next: e => { this.event = e; this.loading = false; },
      error: () => { this.error = 'Event not found'; this.loading = false; }
    });
  }

  back() { this.router.navigate(['/events']); }
}
