import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  @Output() switchMode = new EventEmitter<void>();
  @Output() login = new EventEmitter<{ email: string; password: string }>();
  @Input() error: string | null = null;

  usernameOrEmail: string = '';
  password: string = '';

  constructor(private router: Router) {}

  onSubmit() {
    this.login.emit({ email: this.usernameOrEmail, password: this.password });
  }
}
