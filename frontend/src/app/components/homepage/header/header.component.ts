import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteListComponent } from './note-list/note-list.component';
import { SettingsComponent } from './settings/settings.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, NoteListComponent, SettingsComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  @Output() noteSelected = new EventEmitter<number>();
  @Output() actionSelected = new EventEmitter<string>();

  onNoteSelected(noteId: number) {
    this.noteSelected.emit(noteId);
  }

  onActionSelected(action: string) {
    this.actionSelected.emit(action);
  }
}
