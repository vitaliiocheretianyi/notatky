import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Note } from '../../../../models/note.model';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-note-list',
  standalone: true,
  imports: [CommonModule],
  providers: [DatePipe],
  templateUrl: './note-list.component.html',
  styleUrls: ['./note-list.component.css']
})
export class NoteListComponent {
  @Input() notes: Note[] = [];
  @Output() noteSelected = new EventEmitter<string>();
  @Output() noteDeleted = new EventEmitter<string>(); // Add output event for note deletion

  constructor(private datePipe: DatePipe) {}

  onNoteClick(noteId: string) {
    this.noteSelected.emit(noteId);
  }

  onDeleteClick(noteId: string) {
    this.noteDeleted.emit(noteId); // Emit noteDeleted event with noteId
  }

  getFormattedDate(dateString: string): string {
    return this.datePipe.transform(dateString, 'short') || dateString;
  }
}
