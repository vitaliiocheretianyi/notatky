import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteListComponent } from './note-list/note-list.component';
import { SettingsComponent } from './settings/settings.component';
import { Note } from '../../../models/note.model';
import { NoteService } from '../../../services/note.service'; // Ensure correct import
import { HomepageComponent } from '../homepage.component'; // Import HomepageComponent to access its methods

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, NoteListComponent, SettingsComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  providers: [NoteService] // Add NoteService provider
})
export class HeaderComponent {
  @Output() notesChange = new EventEmitter<Note[]>();
  @Output() noteSelected = new EventEmitter<string>();
  @Input() notes: Note[] = [];
  @Input() homepageComponent!: HomepageComponent; // Input HomepageComponent to call its methods

  constructor(private noteService: NoteService) {} // Inject NoteService

  addNote() {
    const newNote: Partial<Note> = {
      title: 'Untitled', // Default title
      lastInteractedWith: new Date().toISOString()
    };

    this.noteService.createNote(newNote.title!).subscribe(
      (response: any) => {
        const createdNote: Note = {
          id: response.data.id ?? '', // Use an empty string as fallback if response.id is undefined
          title: response.data.title ?? 'Untitled', // Fallback to 'Untitled' if response.title is undefined
          contents: [],
          lastInteractedWith: response.data.lastInteractedWith ?? new Date().toISOString() // Fallback to current date if response.lastInteractedWith is undefined
        };
        this.notes.push(createdNote);
        this.notesChange.emit(this.notes);
        this.noteSelected.emit(createdNote.id);
        this.homepageComponent.loadNotes(); // Call loadNotes method to fetch and render the updated notes list
        if (createdNote.id) {
          this.homepageComponent.loadNoteChildren(createdNote.id); // Load the children of the newly created note
          this.homepageComponent.onNoteSelected(createdNote.id); // Select the newly created note
        }
      },
      (error: any) => {
        console.error('Error creating note:', error);
      }
    );
  }

  selectNote(noteId: string) {
    this.noteSelected.emit(noteId);
  }
}
