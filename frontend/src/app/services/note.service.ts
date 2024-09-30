import { Injectable } from '@angular/core';
import axios, { AxiosResponse } from 'axios';
import { from, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Note, NoteChild } from '../models/note.model';

@Injectable({
  providedIn: 'root',
})
export class NoteService {
  private apiUrl = 'http://localhost:8080/notatky';

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

  // 1. Method to Create a New Note
  createNote(title: string): Observable<any> {
    const url = `${this.apiUrl}/create`;
    const headers = this.getHeaders();
    const lastInteractedWith = new Date().toISOString();
    return from(axios.post(url, { title, lastInteractedWith }, { headers })).pipe(
      catchError((error: any) => this.handleTokenExpiration(error)),
      catchError((error: any) => this.handleError(error))
    );
  }

  editNoteTitle(noteId: string, title: string): Observable<Note> {
    console.log("NEW NOTE TITLE: " + title)
    const url = `${this.apiUrl}/edit`;
    const headers = this.getHeaders();
    const payload = {
      noteId,
      title,
      lastInteractedWith: new Date().toISOString(),
    };

    // Perform the request using axios and map the response to the correct Note type
    return from(axios.put(url, payload, { headers })).pipe(
      map((response: AxiosResponse<any>) => {
        // Assuming response.data contains the note object; adjust if necessary
        const noteData = response.data;
        // Transform the response data into a Note object
        const note: Note = {
          id: noteData.id,
          title: noteData.title,
          contents: noteData.contents || [], // Default to an empty array if not provided
          lastInteractedWith: noteData.lastInteractedWith || new Date().toISOString(),
        };
        return note;
      }),
      catchError((error: any) => this.handleTokenExpiration(error)),
      catchError((error: any) => this.handleError(error))
    );
  }
  // 2. Method to Fetch All Notes
  getAllNotes(): Observable<any> {
    const url = `${this.apiUrl}/notes`;
    const headers = this.getHeaders();
    return from(axios.get(url, { headers })).pipe(
      catchError((error: any) => this.handleTokenExpiration(error)),
      catchError((error: any) => this.handleError(error))
    );
  }

  // 3. Method to Delete a Note
  deleteNote(noteId: string): Observable<any> {
    const url = `${this.apiUrl}/delete`;
    const headers = this.getHeaders();
    return from(axios.delete(url, { headers, data: { noteId } })).pipe(
      catchError((error: any) => this.handleTokenExpiration(error)),
      catchError((error: any) => this.handleError(error))
    );
  }
}
