import { Injectable } from '@angular/core';
import axios from 'axios';
import { from, Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class NoteService {
  private apiUrl = 'http://localhost:8080/notatky';

  constructor() {}

  private getHeaders() {
    const token = this.getToken();
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
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

  createNote(title: string): Observable<any> {
    const url = `${this.apiUrl}/create`;
    const headers = this.getHeaders();
    const lastInteractedWith = new Date().toISOString();
    return from(axios.post(url, { title, lastInteractedWith }, { headers })).pipe(
      catchError(error => this.handleTokenExpiration(error)),
      catchError(error => this.handleError(error))
    );
  }

  getAllNotes(): Observable<any> {
    const url = `${this.apiUrl}/notes`;
    const headers = this.getHeaders();
    return from(axios.get(url, { headers })).pipe(
      catchError(error => this.handleTokenExpiration(error)),
      catchError(error => this.handleError(error))
    );
  }

  getNoteChildren(noteId: string): Observable<any> {
    const url = `${this.apiUrl}/note/${noteId}/children`;
    const headers = this.getHeaders();
    return from(axios.get(url, { headers })).pipe(
      catchError(error => this.handleTokenExpiration(error)),
      catchError(error => this.handleError(error))
    );
  }

  editNoteTitle(noteId: string, title: string): Observable<any> {
    const url = `${this.apiUrl}/edit`;
    const headers = this.getHeaders();
    const lastInteractedWith = new Date().toISOString();
    return from(axios.put(url, { noteId, title, lastInteractedWith }, { headers })).pipe(
      catchError(error => this.handleTokenExpiration(error)),
      catchError(error => this.handleError(error))
    );
  }

  deleteNote(noteId: string): Observable<any> {
    const url = `${this.apiUrl}/delete`;
    const headers = this.getHeaders();
    return from(axios.delete(url, { headers, data: { noteId } })).pipe(
      catchError(error => this.handleTokenExpiration(error)),
      catchError(error => this.handleError(error))
    );
  }

  editTextEntry(noteId: string, textEntryId: string, newText: string, position: number): Observable<any> {
    const url = `${this.apiUrl}/text/edit`;
    const headers = this.getHeaders();
    return from(axios.put(url, { noteId, textEntryId, newText, position }, { headers })).pipe(
      catchError(error => this.handleError(error))
    );
  }

  createTextEntry(noteId: string, text: string, position: number): Observable<any> {
    const url = `${this.apiUrl}/text/create`;
    const headers = this.getHeaders();
    return from(axios.post(url, { noteId, text, position }, { headers })).pipe(
      catchError(error => this.handleError(error))
    );
  }

  deleteTextEntry(noteId: string, textEntryId: string): Observable<any> {
    const url = `${this.apiUrl}/text/delete`;
    const headers = this.getHeaders();
    return from(axios.delete(url, { headers, data: { noteId, textEntryId } })).pipe(
      catchError(error => this.handleTokenExpiration(error)),
      catchError(error => this.handleError(error))
    );
  }

  private handleError(error: any): Observable<never> {
    let errorMessage: string = error.response?.data?.message || error.message || 'An unknown error occurred!';
    console.error('Error:', errorMessage);
    return throwError(errorMessage);
  }
}
