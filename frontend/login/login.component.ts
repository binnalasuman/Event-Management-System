import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  error?: string;

  constructor(private http: HttpClient, private router: Router) {}

  submit() {
  this.http.post<any>('http://localhost:8082/api/users/login', {
    email: this.email,
    password: this.password
  }).subscribe({
    next: res => {
      localStorage.setItem('role', res.role || 'USER');
      if (res.token) localStorage.setItem('token', res.token);
      // Role-based navigation
      this.router.navigate([res.role === 'ADMIN' ? '/admin/events' : '/events']);
    },
    error: () => this.error = 'Invalid email or password'
  });
}

}
