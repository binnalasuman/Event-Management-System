import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventsService } from '../../core/events.service';
import { Event } from '../../core/event';

@Component({
  selector: 'app-events-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './events-list.html',
  styleUrls: ['./events-list.scss']
})
export class EventsListComponent {
  events: Event[] = [];
  loading = false;
  error?: string;

  // Filter state
  title = '';
  location = '';
  sortBy: string = 'eventDate';
  direction: 'asc' | 'desc' = 'asc';

  private role = localStorage.getItem('role') || 'USER';

  constructor(private svc: EventsService, private router: Router) {}

  ngOnInit() { this.load(); }

  isAdmin(): boolean { 
    return (localStorage.getItem('role') || 'USER') === 'ADMIN'; 
  }

  load() {
    this.loading = true;
    this.error = undefined;
    this.svc.list({
      title: this.title,
      location: this.location,
      sortBy: this.sortBy,
      direction: this.direction
    }).subscribe({
      next: res => {
        this.events = Array.isArray(res) ? res : (res?.content ?? []);
        this.loading = false;
      },
      error: () => { 
        this.error = 'Failed to load events'; 
        this.loading = false; 
      }
    });
  }

  applyFilters() { this.load(); }
  clearFilters() { 
    this.title = ''; 
    this.location = ''; 
    this.applyFilters(); 
  }

  setSort(field: string) {
    if (this.sortBy === field) 
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    else { 
      this.sortBy = field; 
      this.direction = 'asc'; 
    }
    this.load();
  }

  // Navigate to admin route for creating new event
  goCreate() {
    if (!this.isAdmin()) return;
    this.router.navigate(['/admin/events/new']);
  }

  // Route based on role (admin vs user)
  open(id?: number) { 
    if (!id) return;
    if (this.isAdmin()) {
      this.router.navigate(['/admin/events', id]);
    } else {
      this.router.navigate(['/events', id]);
    }
  }

  // Navigate to admin edit route
  edit(id?: number) {
    if (!this.isAdmin() || !id) return;
    this.router.navigate(['/admin/events', id, 'edit']);
  }

  viewBookings(id: number) {
    this.router.navigate(['/admin/events', id, 'bookings']);
  }

  // Delete functionality
  remove(id?: number) {
    if (!this.isAdmin() || !id) return;
    if (!confirm('Delete this event?')) return;
    this.svc.remove(id).subscribe({
      next: () => this.load(),
      error: () => this.error = 'Failed to delete event'
    });
  }

  // For users: booking action - redirect to booking page
  book(id?: number) {
    if (!id) return;
    this.router.navigate(['/book', id]);
  }
}
