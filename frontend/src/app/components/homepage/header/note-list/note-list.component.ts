import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-note-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './note-list.component.html',
  styleUrls: ['./note-list.component.css']
})
export class NoteListComponent {
  @Output() noteSelected = new EventEmitter<number>();
  notes = [
    { id: 1, title: 'Note 1' },
    { id: 2, title: 'Note 2' },
    { id: 3, title: 'Note 3' },
    // Add more notes as needed
  ];

  onNoteClick(noteId: number) {
    this.noteSelected.emit(noteId);
  }
}
