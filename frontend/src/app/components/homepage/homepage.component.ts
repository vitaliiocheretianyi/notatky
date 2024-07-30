import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { MainSectionComponent } from './main-section/main-section.component';
import { NoteListComponent } from './header/note-list/note-list.component'; // Ensure correct import
import { NoteService } from '../../services/note.service';
import { Note, NoteChild } from '../../models/note.model';

@Component({
  selector: 'app-homepage',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MainSectionComponent, NoteListComponent], // Add NoteListComponent here
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css'],
  providers: [NoteService]
})
export class HomepageComponent implements OnInit {
  @ViewChild(HeaderComponent) headerComponent!: HeaderComponent; // Reference to HeaderComponent
  @ViewChild(MainSectionComponent) mainSectionComponent!: MainSectionComponent; // Reference to MainSectionComponent
  selectedNoteId: string | null = null;
  notes: Note[] = [];
  noteChildren: NoteChild[] = [];

  constructor(private noteService: NoteService) {}

  ngOnInit() {
    this.loadNotes();
  }

  ngAfterViewInit() {
    this.headerComponent.homepageComponent = this; // Assign HomepageComponent instance to HeaderComponent
  }

  loadNotes() {
    this.noteService.getAllNotes().subscribe({
      next: (response: any) => {
        this.notes = response.data; // Ensure this matches the data structure from backend
        this.sortNotesByLastInteractedWith();
        console.log('Notes received from backend:', this.notes); // Debugging line
      },
      error: (error) => {
        console.error('Error loading notes:', error);
      }
    });
  }

  sortNotesByLastInteractedWith() {
    this.notes.sort((a, b) => new Date(b.lastInteractedWith).getTime() - new Date(a.lastInteractedWith).getTime());
  }

  onNotesChange(updatedNotes: Note[]) {
    this.notes = updatedNotes;
    this.sortNotesByLastInteractedWith();
  }

  onNoteSelected(noteId: string) {
    this.selectedNoteId = noteId;
    this.loadNoteChildren(noteId);
    setTimeout(() => {
      this.mainSectionComponent.loadNote(); // Ensure the selected note is loaded in the main section
    }, 0);
  }

  onNoteDeleted(noteId: string) {
    this.noteService.deleteNote(noteId).subscribe({
      next: () => {
        this.notes = this.notes.filter(note => note.id !== noteId);
        console.log(`Note ${noteId} deleted successfully`);
      },
      error: (error) => {
        console.error('Error deleting note:', error);
      }
    });
  }

  loadNoteChildren(noteId: string) {
    this.noteService.getNoteChildren(noteId).subscribe({
      next: (response: any) => {
        this.noteChildren = response.data;
        console.log(`Note ${noteId} has ${this.noteChildren.length} children.`); // Log number of children
        console.log('Note children received from backend:', this.noteChildren); // Debugging line
        const note = this.notes.find(n => n.id === noteId);
        if (note) {
          note.contents = this.noteChildren; // Update the note's contents with the children
        }
      },
      error: (error) => {
        console.error('Error loading note children:', error);
      }
    });
  }

  onNotesUpdated() {
    this.loadNotes(); // Reload notes when notified
  }

  addNote() {
    const newNoteTitle = 'Untitled'; // Default title
    this.noteService.createNote(newNoteTitle).subscribe({
      next: (response: any) => {
        const newNote: Note = {
          id: response.data.id ?? '', // Use an empty string as fallback if response.id is undefined
          title: response.data.title ?? newNoteTitle, // Fallback to 'Untitled' if response.title is undefined
          contents: [],
          lastInteractedWith: response.data.lastInteractedWith ?? new Date().toISOString() // Fallback to current date if response.lastInteractedWith is undefined
        };
        this.notes.push(newNote);
        this.sortNotesByLastInteractedWith();
        this.onNoteSelected(newNote.id); // Automatically select and open the newly created note
      },
      error: (error) => {
        console.error('Error adding note:', error);
      }
    });
  }
}
