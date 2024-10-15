import { Injectable } from '@angular/core';
import axios from 'axios';
import { Observable, from, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { ChangeEmailRequest, ChangePasswordRequest, ChangeUsernameRequest } from '../models/user.dto';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = 'http://localhost:8080/notatky'; // Backend base URL

  constructor() {}

  private getToken(): string | null {
    return localStorage.getItem('jwtToken');
  }

  private getHeaders() {
    const token = this.getToken();
    return {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    };
  }

  changeUsername(request: ChangeUsernameRequest): Observable<any> {
    return from(axios.put(`${this.baseUrl}/change-username`, request, { headers: this.getHeaders() }))
      .pipe(
        tap(response => console.log('Username changed successfully:', response)),
        catchError(error => this.handleError(error))
      );
  }

  changeEmail(request: ChangeEmailRequest): Observable<any> {
    return from(axios.put(`${this.baseUrl}/change-email`, request, { headers: this.getHeaders() }))
      .pipe(
        tap(response => console.log('Email changed successfully:', response)),
        catchError(error => this.handleError(error))
      );
  }

  changePassword(request: ChangePasswordRequest): Observable<any> {
    return from(axios.put(`${this.baseUrl}/change-password`, request, { headers: this.getHeaders() }))
      .pipe(
        tap(response => console.log('Password changed successfully:', response)),
        catchError(error => this.handleError(error))
      );
  }
  
  deleteAccount(): Observable<any> {
    return from(axios.delete(`${this.baseUrl}/delete-account`, { headers: this.getHeaders() }))
      .pipe(
        tap(() => {
          console.log('Account deleted successfully');
        }),
        catchError(error => {
          console.error('Error deleting account:', error);
          return throwError(error);
        })
      );
  }

  private handleError(error: any): Observable<never> {
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred!';
    return throwError(errorMessage);
  }
}
