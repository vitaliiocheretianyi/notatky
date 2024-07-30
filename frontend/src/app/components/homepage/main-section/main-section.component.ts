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
  originalTitle: string = '';
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
  }

  loadNote() {
    this.initialLoad = true;
    const note = this.notes.find(n => n.id === this.selectedNoteId);
    if (note) {
      this.noteForm.setControl('title', this.fb.control(note.title));
      this.originalTitle = note.title;
      this.loadNoteChildren(note.contents);
      setTimeout(() => {
        this.initialLoad = false;
        this.setFormValueChanges();
      }, 0);
    }
  }

  loadNoteChildren(children: NoteChild[] = []) {
    const contentsArray = this.fb.array([]);
    this.metadata = [];
    this.originalContent = [];
    const sortedChildren = [...children].sort((a, b) => a.position - b.position);
    sortedChildren.forEach(child => {
      if (child.type === 'text' && child.textNode) {
        const control = this.fb.control(child.textNode.content);
        contentsArray.push(control);
        this.metadata.push({ childId: child.textNode.id });
        this.originalContent.push(child.textNode.content);
      } else {
        // Handle image nodes or other types if necessary
      }
    });

    if (contentsArray.length === 0) {
      contentsArray.push(this.fb.control(''));
      this.metadata.push({ childId: null });
      this.originalContent.push('');
    }

    this.noteForm.setControl('contents', contentsArray);
  }

  setFormValueChanges() {
    this.noteForm.get('title')?.valueChanges.pipe(debounceTime(300)).subscribe(() => {
      if (!this.initialLoad) {
        this.handleTitleChange();
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

  addContentField() {
    this.contents.push(this.fb.control(''));
    this.metadata.push({ childId: null });
    this.originalContent.push('');

    setTimeout(() => {
      this.noteContentElements.last.nativeElement.focus();
    }, 0);
  }

  handleKeyDown(event: KeyboardEvent, contentIndex: number) {
    if (event.shiftKey && event.key === 'Enter') {
      event.preventDefault();
      this.addContentField();
    }
  }

  handleTitleChange() {
    if (this.noteForm.value.title !== this.originalTitle) {
      this.saveNoteTitle();
    }
  }

  handleTextChange(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    if (contentControl.value.trim() !== this.originalContent[contentIndex]) {
      this.saveTextEntry(contentIndex);
    }
  }

  saveNoteTitle() {
    if (this.selectedNoteId === null) return;

    this.noteService.editNoteTitle(this.selectedNoteId, this.noteForm.value.title).subscribe(() => {
      this.notesUpdated.emit();
      this.originalTitle = this.noteForm.value.title; // Update the original title after successful save
    }, error => {
      console.error('Error editing note title:', error);
    });
  }

  saveTextEntry(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const noteId = this.selectedNoteId;
    const textEntryId = this.metadata[contentIndex].childId;
    const newText = contentControl.value;

    if (noteId && textEntryId) {
      this.noteService.editTextEntry(noteId, textEntryId, newText, contentIndex).subscribe(() => {
        this.notesUpdated.emit();
        this.originalContent[contentIndex] = newText; // Update the original content after successful save
      }, error => {
        console.error('Error editing text entry:', error);
      });
    } else if (noteId) {
      this.noteService.createTextEntry(noteId, newText, contentIndex).subscribe((response: any) => {
        this.metadata[contentIndex].childId = response.id;
        this.notesUpdated.emit();
        this.originalContent[contentIndex] = newText; // Update the original content after successful save
      }, error => {
        console.error('Error creating text entry:', error);
      });
    }
  }

  logNotes() {
    console.log(this.noteForm.value);
  }

  saveNote() {
    if (this.selectedNoteId === null) return;

    const noteIndex = this.notes.findIndex(n => n.id === this.selectedNoteId);
    if (noteIndex > -1) {
      const updatedNote: Note = {
        id: this.selectedNoteId,
        title: this.noteForm.value.title,
        contents: this.noteForm.value.contents.map((contentControl: FormControl, index: number) => ({
          id: this.metadata[index].childId,
          noteId: this.selectedNoteId,
          textNode: { content: contentControl.value },
          position: index,
          type: 'text'
        })),
        lastInteractedWith: this.notes[noteIndex].lastInteractedWith
      };

      this.notes[noteIndex] = updatedNote;

      if (updatedNote.title !== this.originalTitle) {
        this.saveNoteTitle();
      }

      updatedNote.contents.forEach((content, index) => {
        if (content.id && content.textNode && content.textNode.content !== this.originalContent[index]) {
          this.saveTextEntry(index);
        } else if (!content.id && content.textNode && content.textNode.content.trim() !== '') {
          this.saveTextEntry(index);
        }
      });
    }
  }
}
