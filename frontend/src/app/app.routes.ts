import { Routes } from '@angular/router';
import { HomepageComponent } from './components/homepage/homepage.component';
import { LoginComponent } from './components/auth/login/login.component';
import { AuthGuard } from './services/auth.guard';  // Import the AuthGuard

export const routes: Routes = [
  { path: 'login', component: LoginComponent },  // Public route
  { path: 'home', component: HomepageComponent, canActivate: [AuthGuard] },  // Protected route
  { path: '', redirectTo: '/login', pathMatch: 'full' }  // Redirect to login by default
];
