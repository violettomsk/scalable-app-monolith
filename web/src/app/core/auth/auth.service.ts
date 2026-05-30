import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

// BFF pattern: tokens never reach JavaScript — only an httpOnly session cookie.
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  readonly user = signal<{ name: string } | null>(null);

  login(): void { window.location.href = '/bff/login'; }
  logout(): void { window.location.href = '/bff/logout'; }
  me() { return this.http.get<{ name: string }>('/bff/me', { withCredentials: true }); }
}
