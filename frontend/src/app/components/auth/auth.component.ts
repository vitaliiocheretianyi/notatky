import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, LoginComponent, RegisterComponent],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  isLoginMode = true;
  error: string | null = null;

  constructor(private router: Router) {}

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.error = null;
  }

  onLogin(event: { email: string; password: string }) {
    // Simulate a successful login
    this.router.navigate(['/home']);  // Navigate to homepage
  }

  onRegister(event: { username: string; email: string; password: string }) {
    // Call the register service here
    // this.authService.register(event.username, event.email, event.password).subscribe(
    //   response => { /* handle success */ },
    //   error => { this.error = error.error.message; }
    // );
  }
}
