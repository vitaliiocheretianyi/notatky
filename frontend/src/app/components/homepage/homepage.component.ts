import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { MainSectionComponent } from './main-section/main-section.component';
import { ChangeUsernameComponent } from './change-username/change-username.component';
import { ChangeEmailComponent } from './change-email/change-email.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { Note, NoteChild } from '../../models/note.model';
import { NoteService } from '../../services/note.service';
import { NoteChildService } from '../../services/note-child.service';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service'; // Ensure AuthService is imported

@Component({
  selector: 'app-homepage',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    MainSectionComponent,
    ChangeUsernameComponent,
    ChangeEmailComponent,
    ChangePasswordComponent
  ],
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit, AfterViewInit {
  @ViewChild(HeaderComponent) headerComponent!: HeaderComponent;
  @ViewChild(MainSectionComponent) mainSectionComponent!: MainSectionComponent;

  selectedNoteId: string | null = null;
  notes: Note[] = [];
  noteChildren: NoteChild[] = [];

  currentView: 'main' | 'change-username' | 'change-email' | 'change-password' = 'main';

  constructor(
    private authService: AuthService, 
    private noteService: NoteService,
    private noteChildService: NoteChildService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.authService.authenticateUser(); // Check if user is authenticated
    this.loadNotes();
  }

  ngAfterViewInit() {
    if (this.headerComponent) {
      this.headerComponent.noteSelected.subscribe((noteId: string) => this.onNoteSelected(noteId));
      this.headerComponent.notesUpdated.subscribe(() => this.loadNotes());
      this.headerComponent.actionSelected.subscribe((action: string) => this.handleActionSelected(action));  // Listen for actions from settings
    }
  }

  loadNotes() {
    this.noteService.getAllNotes().subscribe({
      next: (response: any) => {
        this.notes = response.data;
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
        const selectedNote = this.notes.find((note) => note.id === noteId);
        if (selectedNote && this.mainSectionComponent) {
          this.mainSectionComponent.loadNote(selectedNote, this.noteChildren);
        }
      },
      error: (error) => {
        console.error('Error loading note children:', error);
      }
    });
  }

  onNoteSelected(noteId: string) {
    this.selectedNoteId = noteId;
    this.currentView = 'main'; // Switch back to main section when a note is selected
    this.loadNoteChildren(noteId);
  }

  handleActionSelected(action: string) {
    switch (action) {
      case 'change-username':
        this.currentView = 'change-username';
        break;
      case 'change-email':
        this.currentView = 'change-email';
        break;
      case 'change-password':
        this.currentView = 'change-password';
        break;
      case 'delete-account':
        this.deleteAccount();
        break;
      default:
        this.currentView = 'main';
    }
  }

  addNote() {
    this.currentView = 'main';  // Switch back to main section when a new note is added
    this.headerComponent.addNote();
  }

  onNoteTitleUpdated() {
    this.headerComponent.loadNotes();
  }

  onNotesUpdated() {
    this.loadNotes();
  }

  // Add this method to handle account deletion
  deleteAccount() {
    if (confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      this.userService.deleteAccount().subscribe({
        next: () => {
          alert('Account deleted successfully');
          // Redirect the user to the login page or another page after deletion
          window.location.href = '/login';  // You can change this to your app's flow
        },
        error: (err) => {
          console.error('Error deleting account:', err);
          alert('Failed to delete the account. Please try again later.');
        }
      });
    }
  }
}
