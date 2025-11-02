// src/app/register-page.component.ts
import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'USER' ;
}

interface RegisterResponse {
  id: number;
  name: string;
  email: string;
  role: string;
}

const API_BASE = 'http://localhost:8082/api'; // move to environment later

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class RegisterPageComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['USER', [Validators.required]],
  });

  loading = false;
  serverError = '';

  onSubmit() {
    if (this.form.invalid || this.loading) return;
    this.loading = true;
    this.serverError = '';

    const body = this.form.getRawValue() as RegisterRequest;

    this.http.post<RegisterResponse>(`${API_BASE}/users/register`, body).subscribe({
      next: (res) => {
        this.loading = false;
        console.log('Registered:', res);
        // e.g., navigate to login or show a toast here
      },
      error: (err) => {
        this.loading = false;
        this.serverError = err?.status === 0 ? 'Network/CORS error' : (err?.error?.message || 'Registration failed');
      }
    });
  }
}
