import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { MainSectionComponent } from './main-section/main-section.component';
import { NoteListComponent } from './header/note-list/note-list.component';
import { NoteService } from '../../services/note.service';
import { Note, NoteChild } from '../../models/note.model';

@Component({
  selector: 'app-homepage',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MainSectionComponent, NoteListComponent],
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css'],
  providers: [NoteService]
})
export class HomepageComponent implements OnInit {
  @ViewChild(HeaderComponent) headerComponent!: HeaderComponent;
  selectedNoteId: string | null = null;
  notes: Note[] = [];
  noteChildren: NoteChild[] = [];

  constructor(private noteService: NoteService) {}

  ngOnInit() {
    this.loadNotes();
  }

  ngAfterViewInit() {
    this.headerComponent.homepageComponent = this;
  }

  loadNotes() {
    this.noteService.getAllNotes().subscribe({
      next: (response: any) => {
        this.notes = response.data;
        this.sortNotesByLastInteractedWith();
        console.log('Notes received from backend:', this.notes);
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
        console.log(`Note ${noteId} has ${this.noteChildren.length} children.`);
        console.log('Note children received from backend:', this.noteChildren);
        const note = this.notes.find(n => n.id === noteId);
        if (note) {
          note.contents = this.noteChildren;
        }
      },
      error: (error) => {
        console.error('Error loading note children:', error);
      }
    });
  }

  onNotesUpdated() {
    this.loadNotes();
  }

  addNote() {
    const newNoteTitle = prompt('Enter note title:');
    if (newNoteTitle) {
      this.noteService.createNote(newNoteTitle).subscribe({
        next: (response: any) => {
          this.notes.push(response.data);
          this.sortNotesByLastInteractedWith();
          console.log('New note added:', response.data);
        },
        error: (error) => {
          console.error('Error adding note:', error);
        }
      });
    }
  }
}
