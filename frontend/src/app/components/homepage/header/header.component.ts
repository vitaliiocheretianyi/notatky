import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteListComponent } from './note-list/note-list.component';
import { SettingsComponent } from './settings/settings.component';
import { NoteService } from '../../../services/note.service';
import { Note } from '../../../models/note.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, NoteListComponent, SettingsComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  providers: [NoteService]
})
export class HeaderComponent {
  @Input() notes: Note[] = [];
  @Output() noteSelected = new EventEmitter<string>();
  @Output() notesUpdated = new EventEmitter<void>(); // Emits when notes are updated to inform the HomepageComponent
  @Output() actionSelected = new EventEmitter<string>(); // Emit action to switch the component in HomepageComponent

  constructor(private noteService: NoteService) {}

  ngOnInit() {
    this.loadNotes();
  }

  loadNotes() {
    this.noteService.getAllNotes().subscribe({
      next: (response: any) => {
        this.notes = response.data;
        this.sortNotesByLastInteractedWith();
      },
      error: (error) => {
        console.error('Error loading notes:', error);
      }
    });
  }

  sortNotesByLastInteractedWith() {
    this.notes.sort(
      (a, b) =>
        new Date(b.lastInteractedWith).getTime() -
        new Date(a.lastInteractedWith).getTime()
    );
  }

  selectNote(noteId: string) {
    this.noteSelected.emit(noteId); // Emit selected note ID to the HomepageComponent
  }

  deleteNote(noteId: string) {
    this.noteService.deleteNote(noteId).subscribe({
      next: () => {
        this.notes = this.notes.filter((note) => note.id !== noteId);
        this.notesUpdated.emit(); // Notify the HomepageComponent that notes have been updated
      },
      error: (error) => {
        console.error('Error deleting note:', error);
      }
    });
  }

  // Add the missing addNote method here
  addNote() {
    const newNoteTitle = 'Untitled';
    this.noteService.createNote(newNoteTitle).subscribe({
      next: (response: any) => {
        const newNote: Note = {
          id: response.data.id ?? '',
          title: response.data.title ?? newNoteTitle,
          contents: [],
          lastInteractedWith: response.data.lastInteractedWith ?? new Date().toISOString(),
        };
        this.notes.push(newNote);
        this.sortNotesByLastInteractedWith();
        this.noteSelected.emit(newNote.id); // Automatically select the newly created note
        this.notesUpdated.emit(); // Notify the HomepageComponent that notes have been updated
      },
      error: (error) => {
        console.error('Error adding note:', error);
      }
    });
  }

  onActionSelected(action: string) {
    this.actionSelected.emit(action); // Emit action to switch components in HomepageComponent
  }
}
