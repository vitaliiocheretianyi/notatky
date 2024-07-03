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

  onLogin(event: { email: string; password: string }) {
    this.authService.login(event.email, event.password).subscribe(
      response => {
        this.router.navigate(['/home']);
      },
      error => {
        this.error = 'Invalid username/email or password';
      }
    );
  }

  onRegister(event: { username: string; email: string; password: string }) {
    const user: UserDTO = { name: event.username, email: event.email, password: event.password };
    this.authService.register(user).subscribe(
      response => {
        console.log(response);
      },
      error => {
        this.error = error.error.message;
      }
    );
  }
}
