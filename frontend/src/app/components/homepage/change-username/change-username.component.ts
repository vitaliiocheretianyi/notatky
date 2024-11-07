import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-change-username',
  standalone: true, // Declare this component as standalone
  imports: [CommonModule, ReactiveFormsModule], // Import ReactiveFormsModule here
  templateUrl: './change-username.component.html',
  styleUrls: ['./change-username.component.css']
})
export class ChangeUsernameComponent {
  changeUsernameForm: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.changeUsernameForm = this.fb.group({
      newUsername: ['', [Validators.required]]
    });
  }

  onSubmit() {
    if (this.changeUsernameForm.valid) {
      this.userService.changeUsername(this.changeUsernameForm.value).subscribe({
        next: (response) => {
          this.successMessage = 'Username changed successfully!';
          this.errorMessage = '';
        },
        error: (error) => {
          this.errorMessage = error;
          this.successMessage = '';
        }
      });
    }
  }
}
