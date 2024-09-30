// homepage.component.ts

import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { MainSectionComponent } from './main-section/main-section.component';
import { NoteListComponent } from './header/note-list/note-list.component';
import { NoteService } from '../../services/note.service';
import { NoteChildService } from '../../services/note-child.service';
import { Note, NoteChild } from '../../models/note.model';

@Component({
  selector: 'app-homepage',
  standalone: true,
  imports: [CommonModule, HeaderComponent, MainSectionComponent, NoteListComponent],
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css'],
  providers: [NoteService, NoteChildService]
})
export class HomepageComponent implements OnInit, AfterViewInit {
  @ViewChild(HeaderComponent) headerComponent!: HeaderComponent;
  @ViewChild(MainSectionComponent) mainSectionComponent!: MainSectionComponent;
  selectedNoteId: string | null = null;
  notes: Note[] = [];
  noteChildren: NoteChild[] = [];

  constructor(
    private noteService: NoteService,
    private noteChildService: NoteChildService
  ) {}

  ngOnInit() {
    this.loadNotes();
  }

  ngAfterViewInit() {
    if (this.headerComponent) {
      this.headerComponent.homepageComponent = this;
    } else {
      console.error('HeaderComponent is not initialized');
    }
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

  loadNoteChildren(noteId: string) {
    this.noteChildService.getAllNoteChildren(noteId).subscribe({
      next: (response: NoteChild[]) => {
        this.noteChildren = response;
        console.log('Note children received:', this.noteChildren);
      },
      error: (error) => {
        console.error('Error loading note children:', error);
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

  onNotesChange(updatedNotes: Note[]) {
    this.notes = updatedNotes;
    this.sortNotesByLastInteractedWith();
  }

  onNoteSelected(noteId: string) {
    this.selectedNoteId = noteId;
    this.loadNoteChildren(noteId);
    setTimeout(() => {
      if (this.mainSectionComponent) {
        this.mainSectionComponent.loadNote(); // Loads the selected note into the main section
      } else {
        console.error('MainSectionComponent is not initialized');
      }
    }, 0);
  }

  onNoteDeleted(noteId: string) {
    this.noteService.deleteNote(noteId).subscribe({
      next: () => {
        this.notes = this.notes.filter((note) => note.id !== noteId);
        console.log(`Note ${noteId} deleted successfully`);
      },
      error: (error) => {
        console.error('Error deleting note:', error);
      }
    });
  }

  onNotesUpdated() {
    this.loadNotes();
  }

  addNote() {
    const newNoteTitle = 'Untitled';
    this.noteService.createNote(newNoteTitle).subscribe({
      next: (response: any) => {
        const newNote: Note = {
          id: response.data.id ?? '',
          title: response.data.title ?? newNoteTitle,
          contents: [],
          lastInteractedWith:
            response.data.lastInteractedWith ?? new Date().toISOString(),
        };
        this.notes.push(newNote);
        this.sortNotesByLastInteractedWith();
        this.onNoteSelected(newNote.id);
      },
      error: (error) => {
        console.error('Error adding note:', error);
      },
    });
  }
}
