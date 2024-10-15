import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const token = this.authService.getToken();

    if (token && this.authService.isTokenValid(token)) {
      return true;  // Allow access if the token is valid
    } else {
      this.router.navigate(['/login']);  // Redirect to login if token is invalid
      return false;  // Deny access
    }
  }
}
