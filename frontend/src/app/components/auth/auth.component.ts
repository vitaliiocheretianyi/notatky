import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { AuthService } from '../../services/auth.service';
import { UserDTO } from '../../models/user.dto';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, HttpClientModule, LoginComponent, RegisterComponent],
  providers: [AuthService],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  isLoginMode = true;
  error: string | null = null;

  constructor(private router: Router, private authService: AuthService) {}

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.error = null;
  }

  onLogin(event: { identifier: string; password: string }) {
    this.authService.login(event.identifier, event.password).subscribe(
      response => {
        this.router.navigate(['/home']);
      },
      error => {
        this.error = error; // Display the error message in the component
      }
    );
  }

  onRegister(event: { username: string; email: string; password: string }) {
    const user: UserDTO = { username: event.username, email: event.email, password: event.password };
    this.authService.register(user).subscribe(
      response => {
        this.router.navigate(['/home']);
      },
      error => {
        this.error = error; // Display the error message in the component
      }
    );
  }
}
