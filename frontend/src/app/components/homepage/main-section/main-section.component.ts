// main-section.component.ts
import {
  Component,
  Input,
  OnInit,
  Output,
  EventEmitter,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Note, NoteChild, TextNode } from '../../../models/note.model';
import { NoteService } from '../../../services/note.service';
import { NoteChildService } from '../../../services/note-child.service';
import { TextChildComponent } from './text-child/text-child.component';
import { ImageChildComponent } from './image-child/image-child.component';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-main-section',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DragDropModule, TextChildComponent, ImageChildComponent],
  templateUrl: './main-section.component.html',
  styleUrls: ['./main-section.component.css'],
})
export class MainSectionComponent implements OnInit {
  @Input() selectedNoteId: string | null = null;
  @Input() notes: Note[] = [];
  @Input() noteChildren: NoteChild[] = [];
  @Output() notesUpdated = new EventEmitter<void>();

  noteForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private noteService: NoteService,
    private noteChildService: NoteChildService
  ) {}

  ngOnInit() {
    this.initializeForm();
    this.setupRealtimeTitleUpdate();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedNoteId'] && !changes['selectedNoteId'].firstChange) {
      this.loadNote(); // Ensure this loads note and note children
    }
  }
  

  initializeForm() {
    this.noteForm = this.fb.group({
      title: [''],
      contents: this.fb.array([]),
    });
  }

  loadNote() {
    if (!this.selectedNoteId) return;
  
    const note = this.notes.find((n) => n.id === this.selectedNoteId);
    if (note) {
      this.noteForm.patchValue({ title: note.title });
      this.loadNoteChildren(); // Add this line to load the note children
    }
  }

  loadNoteChildren() {
    if (!this.selectedNoteId) return;
  
    this.noteChildService.getAllNoteChildren(this.selectedNoteId).subscribe({
      next: (response: NoteChild[]) => {
        this.noteChildren = response; // Set the noteChildren correctly
        console.log('Note children loaded:', this.noteChildren);
      },
      error: (err) => console.error('Error loading note children:', err),
    });
  }
  

  setupRealtimeTitleUpdate() {
    this.noteForm
      .get('title')
      ?.valueChanges.pipe(debounceTime(500), distinctUntilChanged())
      .subscribe((newTitle) => {
        if (this.selectedNoteId) {
          this.updateNoteTitle(this.selectedNoteId, newTitle);
        }
      });
  }

  updateNoteTitle(noteId: string, newTitle: string) {
    this.noteService.editNoteTitle(noteId, newTitle).subscribe({
      next: () => {
        console.log(`Note title updated to: ${newTitle}`);
        this.notesUpdated.emit();
      },
      error: (err) => console.error('Error updating note title:', err),
    });
  }

  addTextNode() {
    const newTextNode: TextNode = {
      id: null,
      content: '',
    };

    const newNoteChild: NoteChild = {
      id: null,
      position: this.noteChildren.length,
      textNode: newTextNode,
      type: 'text',
    };

    this.noteChildren.push(newNoteChild);
    this.syncNoteChildren();
  }

  syncNoteChildren() {
    if (!this.selectedNoteId) return;
  
    this.noteChildService.syncNoteChildren(this.selectedNoteId, this.noteChildren).subscribe({
      next: (response: any) => {
        // Handle successful sync
        const updatedChildren = response.data;
        if (Array.isArray(updatedChildren)) {
          this.noteChildren = updatedChildren.map((updatedChild: NoteChild) => {
            const existingIndex = this.noteChildren.findIndex((child) => child.id === updatedChild.id);
            if (existingIndex !== -1) {
              // Update the noteChildren list with the backend response (including updated IDs)
              this.noteChildren[existingIndex] = updatedChild;
            } else {
              // Add new child if it was created during sync
              this.noteChildren.push(updatedChild);
            }
            return updatedChild;
          });
          console.log('Note children synced successfully:', this.noteChildren);
        }
      },
      error: (err) => {
        console.error('Error syncing note children:', err);
      }
    });
  }
  

  // main-section.component.ts
  onTextUpdated(updatedChild: NoteChild) {
    const index = this.noteChildren.findIndex((child) => child.id === updatedChild.id);
    if (index !== -1) {
      // Update the noteChildren array with the new content
      this.noteChildren[index] = { ...this.noteChildren[index], textNode: updatedChild.textNode };

      // Sync with the backend to ensure the text node content is updated on the server
      this.syncNoteChildren();
    }
  }

  
  trackByNoteChildId(index: number, item: NoteChild) {
    return item.id; // or item.childId if it's more appropriate
  }
  
  

  onTextDeleted(childId: string) {
    this.noteChildren = this.noteChildren.filter((child) => child.id !== childId);
    this.syncNoteChildren();
  }

  onImageDeleted(childId: string) {
    this.noteChildren = this.noteChildren.filter((child) => child.id !== childId);
    this.syncNoteChildren();
  }

  // The parameter should be of type CdkDragDrop<NoteChild[]>
  drop(event: CdkDragDrop<NoteChild[]>) {
    // Move the item in the array based on the event data
    moveItemInArray(this.noteChildren, event.previousIndex, event.currentIndex);

    // Update positions after reordering
    this.updateNoteChildrenPositions();

    // Sync reordered note children with the backend
    this.syncNoteChildren();
  }
  
  updateNoteChildrenPositions() {
    // Recalculate the position based on the new order
    this.noteChildren.forEach((child, index) => {
      child.position = index; // Update position based on the new index
    });
  }
}

