import { Component } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';  // Ensure these imports

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],  // Only import RouterOutlet here
  template: '<router-outlet></router-outlet>',  // This is the root for all your routed components
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'frontend';
}
