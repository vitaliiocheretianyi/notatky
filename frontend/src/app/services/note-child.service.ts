// note-child.service.ts
import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { from, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { NoteChild } from '../models/note.model';

@Injectable({
  providedIn: 'root',
})
export class NoteChildService {
  private apiUrl = 'http://localhost:8080/notatky/note-children';

  constructor() {}

  private getHeaders() {
    const token = this.getToken();
    return {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    };
  }

  private getToken(): string | null {
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem('jwtToken');
    }
    return null;
  }

  private handleTokenExpiration(error: any): Observable<never> {
    if (error.response && error.response.status === 401) {
      console.error('Token expired or unauthorized');
    }
    return throwError(error);
  }

  private handleError(error: any): Observable<never> {
    let errorMessage: string =
      error.response?.data?.message || error.message || 'An unknown error occurred!';
    console.error(errorMessage);
    return throwError(errorMessage);
  }

  // Method to fetch all note children for a specific note
  getAllNoteChildren(noteId: string): Observable<NoteChild[]> {
    const url = `${this.apiUrl}/all/${noteId}`;
    const headers = this.getHeaders();
    return from(axios.get(url, { headers })).pipe(
      map((response: AxiosResponse<NoteChild[]>) => response.data),
      catchError((error: any) => this.handleTokenExpiration(error)),
      catchError((error: any) => this.handleError(error))
    );
  }

  // Method to sync note children (create, update, delete)
  syncNoteChildren(noteId: string, noteChildren: NoteChild[]): Observable<any> {
    const url = `${this.apiUrl}/sync/${noteId}`;
    const headers = this.getHeaders();
    const payload = { noteChildren };
    return from(axios.post(url, payload, { headers })).pipe(
      catchError((error: any) => this.handleTokenExpiration(error)),
      catchError((error: any) => this.handleError(error))
    );
  }
}
