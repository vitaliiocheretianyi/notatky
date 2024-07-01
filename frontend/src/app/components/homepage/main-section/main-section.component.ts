import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-main-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './main-section.component.html',
  styleUrls: ['./main-section.component.css']
})
export class MainSectionComponent {
  @Input() selectedNoteId: number | null = null;
  @Input() selectedAction: string | null = null;

  get noteContent() {
    return `Content of note ${this.selectedNoteId}`;
  }
}
