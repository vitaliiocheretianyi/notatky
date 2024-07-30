import { Injectable } from '@angular/core';
import axios from 'axios';
import { Observable, from, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { UserDTO } from '../models/user.dto';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080';
  private token: string | null = null;

  constructor() {}

  login(identifier: string, password: string): Observable<any> {
    const isEmail = identifier.includes('@');
    const loginData = isEmail ? { email: identifier, password } : { username: identifier, password };

    return from(axios.post(`${this.baseUrl}/login`, loginData, { headers: { 'Content-Type': 'application/json' } }))
      .pipe(
        tap((response: any) => {
          const token = response.data.data; // Extract the token from response.data.data
          console.log(`Token: ${token}`);
          this.setToken(token);
        }),
        catchError(error => this.handleError(error))
      );
  }

  register(user: UserDTO): Observable<any> {
    return from(axios.post(`${this.baseUrl}/register`, user, { headers: { 'Content-Type': 'application/json' } }))
      .pipe(
        tap((response: any) => {
          const token = response.data.data; // Extract the token from response.data.data
          console.log(`Token: ${token}`);
          this.setToken(token);
        }),
        catchError(error => this.handleError(error))
      );
  }

  private setToken(token: string) {
    this.token = token;
    localStorage.setItem('jwtToken', token);
  }

  getToken(): string | null {
    if (!this.token) {
      this.token = localStorage.getItem('jwtToken');
    }
    return this.token;
  }

  private handleError(error: any): Observable<never> {
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred!';
    return throwError(errorMessage);
  }

  logout(): void {
    this.token = null;
    localStorage.removeItem('jwtToken');
  }
}
