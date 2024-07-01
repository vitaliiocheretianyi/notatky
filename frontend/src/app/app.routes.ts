import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { HomepageComponent } from './components/homepage/homepage.component';

export const routes: Routes = [
  { path: '', component: AuthComponent },
  { path: 'home', component: HomepageComponent },
  // other routes
];
