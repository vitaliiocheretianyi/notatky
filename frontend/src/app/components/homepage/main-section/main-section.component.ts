import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChildren, QueryList, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, FormControl } from '@angular/forms';
import { debounceTime } from 'rxjs/operators';
import { NoteService } from '../../../services/note.service';
import { Note, NoteChild } from '../../../models/note.model';

@Component({
  selector: 'app-main-section',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './main-section.component.html',
  styleUrls: ['./main-section.component.css'],
  providers: [NoteService]
})
export class MainSectionComponent implements OnInit, OnChanges {
  @Input() selectedNoteId: string | null = null;
  @Input() notes: Note[] = [];
  @Input() noteChildren: NoteChild[] = [];
  @Output() notesUpdated = new EventEmitter<void>();

  noteForm!: FormGroup;
  metadata: { childId: string | null }[] = [];
  originalContent: string[] = [];
  initialLoad: boolean = true;
  @ViewChildren('noteContent') noteContentElements!: QueryList<ElementRef>;

  constructor(private fb: FormBuilder, private noteService: NoteService) {}

  ngOnInit() {
    this.initializeForm();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedNoteId'] && this.selectedNoteId !== null) {
      this.loadNote();
    }
    if (changes['noteChildren'] && !changes['noteChildren'].isFirstChange()) {
      this.loadNoteChildren(this.noteChildren);
    }
  }

  initializeForm() {
    this.noteForm = this.fb.group({
      title: [''],
      contents: this.fb.array([])
    });
    this.setFormValueChanges();
  }

  loadNote() {
    if (!this.selectedNoteId) return;

    this.initialLoad = true;
    const note = this.notes.find(n => n.id === this.selectedNoteId);
    if (note) {
      this.noteForm.setControl('title', this.fb.control(note.title));
      
      this.noteService.getNoteChildren(this.selectedNoteId).subscribe({
        next: (response: any) => {
          this.loadNoteChildren(response.data);
          setTimeout(() => this.initialLoad = false, 0);
        },
        error: (error: any) => console.error('Error loading note children:', error)
      });
    }
  }

  loadNoteChildren(children: NoteChild[] = []) {
    const contentsArray = this.fb.array([]);
    this.metadata = [];
    this.originalContent = [];
    
    children.sort((a, b) => a.position - b.position).forEach((child, index) => {
      if (child.type === 'text' && child.textNode) {
        const control = this.fb.control(child.textNode.content);
        contentsArray.push(control);
        this.metadata.push({ childId: child.textNode.id });
        this.originalContent.push(child.textNode.content);
      }
    });

    // Ensure at least one empty content field exists
    if (contentsArray.length === 0) {
      this.addEmptyContentField(contentsArray);
    }

    this.noteForm.setControl('contents', contentsArray);
    this.setFormValueChanges();
  }

  setFormValueChanges() {
    if (!this.noteForm) return;

    this.noteForm.get('title')?.valueChanges.pipe(debounceTime(300)).subscribe(() => {
      if (!this.initialLoad) {
        this.saveNoteTitle();
      }
    });

    this.contents.controls.forEach((control, index) => {
      control.valueChanges.pipe(debounceTime(300)).subscribe(() => {
        if (!this.initialLoad) {
          this.handleTextChange(index);
        }
      });
    });
  }

  get contents() {
    return this.noteForm.get('contents') as FormArray;
  }

  handleKeyDown(event: KeyboardEvent, contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    if (event.shiftKey && event.key === 'Enter') {
      event.preventDefault();
      this.addContentField(contentIndex + 1);
    } else if ((event.key === 'Backspace' || event.key === 'Delete') && contentControl.value.trim() === '') {
      event.preventDefault();
      this.deleteContentField(contentIndex);
    }
  }

  addContentField(position: number) {
    const control = this.fb.control('');
    this.contents.insert(position, control);
    this.metadata.splice(position, 0, { childId: null });
    this.originalContent.splice(position, 0, '');
    this.notesUpdated.emit();
    this.setFormValueChanges();

    setTimeout(() => this.noteContentElements.toArray()[position].nativeElement.focus(), 0);
  }

  deleteContentField(contentIndex: number) {
    const noteId = this.selectedNoteId;
    const textEntryId = this.metadata[contentIndex].childId;

    if (noteId && textEntryId) {
      this.noteService.deleteTextEntry(noteId, textEntryId).subscribe(() => {
        this.contents.removeAt(contentIndex);
        this.metadata.splice(contentIndex, 1);
        this.originalContent.splice(contentIndex, 1);
        this.addEmptyContentFieldIfNecessary(); // Check if an empty field needs to be added
        this.notesUpdated.emit();
      }, (error: any) => console.error('Error deleting text entry:', error));
    } else {
      this.contents.removeAt(contentIndex);
      this.metadata.splice(contentIndex, 1);
      this.originalContent.splice(contentIndex, 1);
      this.addEmptyContentFieldIfNecessary(); // Check if an empty field needs to be added
    }
  }

  handleTextChange(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const newText = contentControl.value.trim();

    if (newText !== this.originalContent[contentIndex]) {
      if (!this.metadata[contentIndex].childId) {
        this.createTextEntry(contentIndex);
      } else {
        this.saveTextEntry(contentIndex);
      }
    }
  }

  createTextEntry(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const noteId = this.selectedNoteId;
    const newText = contentControl.value.trim();

    if (noteId && newText !== '') {
      this.noteService.createTextEntry(noteId, newText, contentIndex).subscribe((response: any) => {
        this.metadata[contentIndex].childId = response.id;
        this.originalContent[contentIndex] = newText;
        this.reloadNoteChildren(noteId); // Reload the note's children to reflect the new state
      }, (error: any) => console.error('Error creating text entry:', error));
    }
  }

  saveTextEntry(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const noteId = this.selectedNoteId;
    const textEntryId = this.metadata[contentIndex].childId;
    const newText = contentControl.value.trim();

    if (noteId && textEntryId) {
      this.noteService.editTextEntry(noteId, textEntryId, newText, contentIndex).subscribe(() => {
        this.originalContent[contentIndex] = newText;
        this.notesUpdated.emit();
      }, (error: any) => console.error('Error editing text entry:', error));
    }
  }

  saveNoteTitle() {
    if (this.selectedNoteId) {
      this.noteService.editNoteTitle(this.selectedNoteId, this.noteForm.value.title).subscribe(() => {
        this.notesUpdated.emit();
      }, (error: any) => console.error('Error editing note title:', error));
    }
  }

  reloadNoteChildren(noteId: string) {
    this.noteService.getNoteChildren(noteId).subscribe({
      next: (response: any) => {
        this.loadNoteChildren(response.data);
        // Focus on the last content field after reloading
        setTimeout(() => {
          const lastIndex = this.contents.length - 1;
          this.noteContentElements.toArray()[lastIndex].nativeElement.focus();
        }, 0);
      },
      error: (error: any) => console.error('Error reloading note children:', error)
    });
  }

  addEmptyContentFieldIfNecessary() {
    if (this.contents.length === 0) {
      this.addEmptyContentField(this.contents);
    }
  }

  addEmptyContentField(contentsArray: FormArray) {
    const control = this.fb.control('');
    contentsArray.push(control);
    this.metadata.push({ childId: null });
    this.originalContent.push('');
  }
}
