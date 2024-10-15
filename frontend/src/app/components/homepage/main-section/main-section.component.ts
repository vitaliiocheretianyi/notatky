import { Component, Input, OnInit, Output, EventEmitter, SimpleChanges, ViewChildren, QueryList, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { NoteChildService } from '../../../services/note-child.service';
import { Note, NoteChild, TextNode } from '../../../models/note.model';
import { TextChildComponent } from './text-child/text-child.component';
import { ImageChildComponent } from './image-child/image-child.component';
import { NoteService } from '../../../services/note.service';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-main-section',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DragDropModule,
    TextChildComponent,
    ImageChildComponent,
  ],
  templateUrl: './main-section.component.html',
  styleUrls: ['./main-section.component.css'],
})

export class MainSectionComponent implements OnInit, AfterViewInit {
  @Input() selectedNoteId: string | null = null;
  @Input() notes: Note[] = [];
  @Input() noteChildren: NoteChild[] = [];
  @Output() notesUpdated = new EventEmitter<void>();

  @ViewChildren(TextChildComponent) textChildComponents!: QueryList<TextChildComponent>;

  newTextNodeIndex: number | null = null;
  previousNoteChildren: NoteChild[] = [];  // Store previous state of noteChildren
  noteForm!: FormGroup;

  // Add this property
  viewChildrenPopulated: boolean = false; // To track if ViewChildren is populated

  constructor(
    private fb: FormBuilder,
    private noteService: NoteService,
    private noteChildService: NoteChildService,
    private cdRef: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.initializeForm();
    this.setupRealtimeTitleUpdate();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedNoteId'] && !changes['selectedNoteId'].firstChange) {
      const selectedNote = this.notes.find(note => note.id === this.selectedNoteId);
      if (selectedNote) {
        this.loadNote(selectedNote, this.noteChildren); // Pass both the selected note and note children
      }
    }
  }
  

  ngAfterViewInit() {
    this.textChildComponents.changes.subscribe(() => {
      this.cdRef.detectChanges();
      if (this.newTextNodeIndex !== null) {
        const textComponentsArray = this.textChildComponents.toArray();
        const newTextChildComponent = textComponentsArray[this.newTextNodeIndex!];
        if (newTextChildComponent) {
          setTimeout(() => {
            newTextChildComponent.focusTextArea();
          }, 0);
        }
        this.newTextNodeIndex = null;
      }
    });
  }
  
  ngAfterViewChecked() {
    // Set the flag once ViewChildren is populated
    if (this.textChildComponents.length > 0 && !this.viewChildrenPopulated) {
      this.viewChildrenPopulated = true;
      console.log('ViewChildren now populated:', this.textChildComponents.length);
    }
  }

  addOrInsertTextChild(index?: number) {
    this.previousNoteChildren = [...this.noteChildren];  // Clone the array
  
    const newTextNode: TextNode = { id: null, content: '' };
  
    const newNoteChild: NoteChild = {
      id: null,
      position: (index !== undefined ? index + 1 : this.noteChildren.length),
      textNode: newTextNode,
      type: 'text',
    };
  
    if (index !== undefined) {
      this.noteChildren.splice(index + 1, 0, newNoteChild);
    } else {
      this.noteChildren.push(newNoteChild);
    }
  
    this.updateNoteChildrenPositions();
    this.syncNoteChildren(() => {
      this.focusNewTextarea();
    });
  }

  focusNewTextarea() {
    const newTextArea = this.noteChildren.find((newChild) => {
      return (
        newChild.type === 'text' && 
        !this.previousNoteChildren.some(oldChild => oldChild.id === newChild.id)
      );
    });
  
    if (newTextArea) {
      const textNodes = this.noteChildren.filter(child => child.type === 'text');
      const newIndex = textNodes.indexOf(newTextArea);
      this.newTextNodeIndex = newIndex;
    } else {
      this.newTextNodeIndex = null;
    }
  }

  handleRequestFocusOrDeletePreviousNode(currentIndex: number) {
    if (currentIndex > 0) {
      const previousChild = this.noteChildren[currentIndex - 1];
      if (previousChild.type === 'text') {
        // Focus on the previous text node
        setTimeout(() => {
          const textComponent = this.textChildComponents.find(comp => comp.textChild.id === previousChild.id);
          if (textComponent) {
            textComponent.focusTextArea('end');
          } else {
            console.error('Could not find previous text component');
          }
        }, 0);
      } else if (previousChild.type === 'image') {
        // Delete the image node using existing functionality
        this.onImageDeleted(previousChild.id!);
      }
    } else {
      console.warn('No previous node to focus or delete');
    }
  }
  
  syncNoteChildren(callback?: () => void) {
    if (!this.selectedNoteId) return;
  
    this.noteChildService.syncNoteChildren(this.selectedNoteId, this.noteChildren).subscribe({
      next: (response: any) => {
        const updatedChildren = response.data;
        if (Array.isArray(updatedChildren)) {
          this.noteChildren = updatedChildren.map((updatedChild: NoteChild) => {
            const existingIndex = this.noteChildren.findIndex((child) => child.id === updatedChild.id);
            if (existingIndex !== -1) {
              this.noteChildren[existingIndex] = updatedChild;
            } else {
              this.noteChildren.push(updatedChild);
            }
            return updatedChild;
          });
          if (callback) {
            callback();
          }
        }
      },
      error: (err) => console.error('Error syncing note children:', err)
    });
  }

  initializeForm() {
    this.noteForm = this.fb.group({
      title: [''],
      contents: this.fb.array([]),
    });
  }
  
  loadNote(note: Note, noteChildren: NoteChild[]) {
    // Load the note data into the form
    this.noteForm.patchValue({
      title: note.title,
      contents: noteChildren,
    });
  
    // Update the internal noteChildren array
    this.noteChildren = noteChildren;
    console.log('Note and children loaded into MainSection:', note, noteChildren);
  }
  

  loadNoteChildren() {
    if (!this.selectedNoteId) return;
    this.noteChildService.getAllNoteChildren(this.selectedNoteId).subscribe({
      next: (response: NoteChild[]) => { this.noteChildren = response; },
      error: (err) => console.error('Error loading note children:', err)
    });
  }

  setupRealtimeTitleUpdate() {
    this.noteForm.get('title')?.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe((newTitle) => {
      if (this.selectedNoteId) {
        // Emit event to notify HomepageComponent of title update
        this.updateNoteTitle(newTitle);
      }
    });
  }

  updateNoteTitle(newTitle: string) {
    // Assuming update logic with the service
    if (this.selectedNoteId) {
      // Trigger backend update and notify parent component (HomepageComponent)
      this.notesUpdated.emit(); // Notify HomepageComponent to reload notes in header
    }
  }

  onTextUpdated(updatedChild: NoteChild) {
    const index = this.noteChildren.findIndex((child) => child.id === updatedChild.id);
    if (index !== -1) {
      this.noteChildren[index] = { ...this.noteChildren[index], textNode: updatedChild.textNode };
      this.syncNoteChildren();
    }
  }

  trackByNoteChildId(index: number, item: NoteChild) {
    return item.id;
  }

  onTextDeleted(childId: string) {
    const index = this.noteChildren.findIndex(child => child.id === childId);
  
    if (index !== -1) {
      // Remove the text node
      this.noteChildren.splice(index, 1);
      
      // Update the positions of the remaining note children
      this.updateNoteChildrenPositions();
      
      // Sync note children with the backend
      this.syncNoteChildren();
  
      // Move focus to the previous node (if any)
      if (index > 0) {
        this.focusOnPreviousTextChild(index);
      }
    }
  }
  
  onImageDeleted(childId: string) {
    const index = this.noteChildren.findIndex(child => child.id === childId);
    if (index !== -1) {
      this.noteChildren.splice(index, 1);
      this.updateNoteChildrenPositions();
      this.syncNoteChildren();
    }
  }

  drop(event: CdkDragDrop<NoteChild[]>) {
    moveItemInArray(this.noteChildren, event.previousIndex, event.currentIndex);
    this.updateNoteChildrenPositions();
    this.syncNoteChildren();
  }

  updateNoteChildrenPositions() {
    this.noteChildren.forEach((child, index) => {
      child.position = index;
    });
  }

  onImageUpload(file: File, noteChildId: string) {
    if (!this.selectedNoteId || !noteChildId) return;
    const formData = new FormData();
    formData.append('image', file, file.name);
    this.noteChildService.uploadImage(this.selectedNoteId, noteChildId, formData)
      .then((updatedNoteChildren: NoteChild[]) => {
        const index = this.noteChildren.findIndex(child => child.id === noteChildId);
        if (index !== -1) {
          this.noteChildren[index] = updatedNoteChildren.find(child => child.id === noteChildId)!;
        }
        this.syncNoteChildren();
      })
      .catch((err: any) => console.error('Error uploading image:', err));
  }
  
  getTextChildComponentByPosition(position: number): TextChildComponent | null {
    if (!this.textChildComponents || this.textChildComponents.length === 0) {
      console.error('ERROR: ViewChildren not populated yet.');
      console.log(this.noteChildren)
      return null;
    }
  
    // Filter only text nodes
    const textNodes = this.noteChildren.filter(child => child.type === 'text');
    
    if (textNodes.length > position && position >= 0) {
      const textComponentsArray = this.textChildComponents.toArray();
  
      // Make sure the textComponentsArray has the same length as the filtered textNodes array
      if (textComponentsArray.length > position) {
        return textComponentsArray[position] || null;
      }
    }
  
    console.error('Could not find text node at position:', position);
    return null;
  }

  focusOnPreviousTextChild(currentPosition: number) {
    // Directly access noteChildren based on position instead of using ViewChildren
    if (currentPosition > 0) {
      const previousNoteChild = this.noteChildren[currentPosition - 1];
  
      if (previousNoteChild && previousNoteChild.type === 'text') {
        // Access the previous text node and manually manage focus
        const textNodes = this.textChildComponents.toArray(); // This will still exist for managing components
        
        const prevTextChildComponent = textNodes.find(comp => comp.textChild.position === previousNoteChild.position);
  
        if (prevTextChildComponent) {
          prevTextChildComponent.focusTextArea('end');
        } else {
          console.error('ERROR: Could not find component for previous text child at position:', currentPosition - 1);
        }
      } else {
        console.error('ERROR: Previous note child is not a text node or out of bounds.');
      }
    }
  }
}
