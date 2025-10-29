import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventsService } from '../../core/events.service';
import { Event } from '../../core/event';

@Component({
  selector: 'app-event-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './event-form.html',
  styleUrls: ['./event-form.scss']
})
export class EventFormComponent {
  id?: number;
  model: Event = { title: '', description: '', location: '', eventDate: '', availableSeats: 1 };
  loading = false;
  error?: string;

  constructor(private route: ActivatedRoute, private router: Router, private svc: EventsService) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && this.route.snapshot.routeConfig?.path?.endsWith('edit')) {
      this.id = +idParam;
      this.loading = true;
      this.svc.get(this.id).subscribe({
        next: e => { this.model = e; this.loading = false; },
        error: () => { this.error = 'Failed to load event'; this.loading = false; }
      });
    }
  }

  save() {
    this.loading = true;
    const req = this.id ? this.svc.update(this.id, this.model) : this.svc.create(this.model);
    req.subscribe({
      next: () => this.router.navigate(['/events']),
      error: () => { this.error = 'Save failed'; this.loading = false; }
    });
  }
}
