import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  @Output() switchMode = new EventEmitter<void>();
  @Output() login = new EventEmitter<{ identifier: string; password: string }>();
  @Input() error: string | null = null;

  identifier: string = '';
  password: string = '';

  onSubmit() {
    this.login.emit({ identifier: this.identifier, password: this.password });
  }
}
