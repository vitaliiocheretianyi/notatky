export interface UserDTO {
    username: string;
    email: string;
    password: string;
  }
  
  export interface ChangeUsernameRequest {
    newUsername: string;
  }
  
  export interface ChangeEmailRequest {
    newEmail: string;
  }
  
  export interface ChangePasswordRequest {
    newPassword: string;
  }
  