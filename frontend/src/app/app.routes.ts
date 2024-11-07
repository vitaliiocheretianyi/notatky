import { Routes } from '@angular/router';
import { HomepageComponent } from './components/homepage/homepage.component';
import { LoginComponent } from './components/auth/login/login.component';
import { AuthGuard } from './services/auth.guard';  // Import the AuthGuard
import { AuthComponent } from './components/auth/auth.component';

export const routes: Routes = [
  { path: '', component: AuthComponent },  // Public route
  { path: 'home', component: HomepageComponent, canActivate: [AuthGuard] },  // Protected route
  { path: '', redirectTo: '/', pathMatch: 'full' }  // Redirect to login by default
];
