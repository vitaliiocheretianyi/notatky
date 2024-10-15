import { Injectable } from '@angular/core';
import axios from 'axios';
import { Observable, from, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { UserDTO } from '../models/user.dto';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080';
  private token: string | null = null;

  constructor() {}

  register(user: UserDTO): Observable<any> {
    return from(axios.post(`${this.baseUrl}/register`, user, { headers: { 'Content-Type': 'application/json' } }))
      .pipe(
        tap((response: any) => {
          const token = response.data.data;
          this.setToken(token);
        }),
        catchError(error => this.handleError(error))
      );
  }

  login(identifier: string, password: string): Observable<any> {
    const isEmail = identifier.includes('@');
    const loginData = isEmail ? { email: identifier, password } : { username: identifier, password };

    return from(axios.post(`${this.baseUrl}/login`, loginData, { headers: { 'Content-Type': 'application/json' } }))
      .pipe(
        tap((response: any) => {
          const token = response.data.data;
          this.setToken(token);
        }),
        catchError(error => this.handleError(error))
      );
  }

  private setToken(token: string) {
    this.token = token;
    localStorage.setItem(this.token, token);
  }

  authenticateUser() {
    const token = this.getToken();
    if (!this.isTokenValid(token)) {
      this.logout();  // If token is invalid, log out and redirect
    }
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const isTokenExpired = Date.now() >= payload.exp * 1000;
      return !isTokenExpired;
    } catch (error) {
      return false;
    }
  }

  getToken(): string | null {
    if (!this.token) {
     // this.token = localStorage.getItem(this.token);
    }
    return this.token;
  }

  isTokenValid(token: string | null): boolean {
    if (!token) return false;
    try {
      const decodedToken: any = jwtDecode(token);  // Decode token using jwtDecode
      const expirationDate = new Date(decodedToken.exp * 1000);
      return expirationDate > new Date();  // Check if token has expired
    } catch (error) {
      return false;
    }
  }

  logout(): void {
    this.token = null;
   // localStorage.removeItem(this.tokenKey);  // Remove token from localStorage
  }

  private handleError(error: any): Observable<never> {
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred!';
    return throwError(errorMessage);
  }
}
