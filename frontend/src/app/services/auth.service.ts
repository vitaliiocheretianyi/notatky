import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDTO } from '../models/user.dto';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/users';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    const loginRequest = { usernameOrEmail: email, password: password };
    return this.http.post(`${this.baseUrl}/login`, loginRequest, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    });
  }

  register(user: UserDTO): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, user, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    });
  }
}
