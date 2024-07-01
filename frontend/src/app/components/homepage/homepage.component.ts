import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { MainSectionComponent } from './main-section/main-section.component';

@Component({
  selector: 'app-homepage',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MainSectionComponent],
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent {
  selectedNoteId: number | null = null;
  selectedAction: string | null = null;

  onNoteSelected(noteId: number) {
    this.selectedNoteId = noteId;
    this.selectedAction = null;
  }

  onActionSelected(action: string) {
    this.selectedAction = action;
    this.selectedNoteId = null;
  }
}
