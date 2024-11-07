import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-change-email',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-email.component.html',
  styleUrls: ['./change-email.component.css']
})
export class ChangeEmailComponent {
  changeEmailForm: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.changeEmailForm = this.fb.group({
      newEmail: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.changeEmailForm.valid) {
      this.userService.changeEmail(this.changeEmailForm.value).subscribe({
        next: (response) => {
          this.successMessage = 'Email changed successfully!';
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
