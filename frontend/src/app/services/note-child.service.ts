// note-child.service.ts (Angular Service)
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
      Authorization: `Bearer ${token}`,
    };
  }

  private getToken(): string | null {
    return typeof localStorage !== 'undefined' ? localStorage.getItem('jwtToken') : null;
  }

  private handleError(error: any): Observable<never> {
    const errorMessage: string =
      error.response?.data?.message || error.message || 'An unknown error occurred!';
    console.error(errorMessage);
    return throwError(errorMessage);
  }

  uploadImage(noteId: string, noteChildId: string, formData: FormData): Promise<NoteChild[]> {
    return axios
      .post(`${this.apiUrl}/upload/${noteId}/${noteChildId}`, formData, {
        headers: {
          ...this.getHeaders(),
          'Content-Type': 'multipart/form-data',
        },
      })
      .then((response: AxiosResponse<NoteChild[]>) => response.data)
      .catch((error: any) => {
        console.error('Error uploading image:', error);
        throw error;
      });
  }
  

  getAllNoteChildren(noteId: string): Observable<NoteChild[]> {
    const url = `${this.apiUrl}/all/${noteId}`;
    const headers = this.getHeaders();
    return from(axios.get(url, { headers })).pipe(
      map((response: AxiosResponse<NoteChild[]>) => response.data),
      catchError((error: any) => this.handleError(error))
    );
  }

  syncNoteChildren(noteId: string, noteChildren: NoteChild[]): Observable<any> {
    const url = `${this.apiUrl}/sync/${noteId}`;
    const headers = this.getHeaders();
    const payload = { noteChildren };
    return from(axios.post(url, payload, { headers })).pipe(
      catchError((error: any) => this.handleError(error))
    );
  }
}
