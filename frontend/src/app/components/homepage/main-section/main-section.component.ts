import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChildren, QueryList, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, FormControl } from '@angular/forms';
import { debounceTime } from 'rxjs/operators';
import { NoteService } from '../../../services/note.service';
import { Note, NoteChild } from '../../../models/note.model';
import { DropdownComponent } from '../dropdown/dropdown.component';

@Component({
  selector: 'app-main-section',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DropdownComponent],
  templateUrl: './main-section.component.html',
  styleUrls: ['./main-section.component.css'],
  providers: [NoteService]
})
export class MainSectionComponent implements OnInit, OnChanges {
  @Input() selectedNoteId: string | null = null;
  @Input() notes: Note[] = [];
  @Input() noteChildren: NoteChild[] = []; // Declare as Input property
  @Output() notesUpdated = new EventEmitter<void>();

  noteForm!: FormGroup;
  metadata: { childId: string | null }[] = [];
  originalContent: string[] = [];
  originalTitle: string = '';
  initialLoad: boolean = true;
  showDropdown: boolean = false;
  highlightedIndex: number = 0;
  dropdownOptions: string[] = ['/image'];
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
    this.setFormValueChanges(); // Initialize value changes detection
  }

  loadNote() {
    if (!this.selectedNoteId) return;

    this.initialLoad = true;
    const note = this.notes.find(n => n.id === this.selectedNoteId);
    if (note) {
      this.noteForm.setControl('title', this.fb.control(note.title));
      this.originalTitle = note.title;

      this.noteService.getNoteChildren(this.selectedNoteId).subscribe({
        next: (response: any) => {
          console.log('Note children received:', response.data); // Log note children
          this.loadNoteChildren(response.data);
          setTimeout(() => {
            this.initialLoad = false;
          }, 0);
        },
        error: (error: any) => {
          console.error('Error loading note children:', error);
        }
      });
    }
  }

  loadNoteChildren(children: NoteChild[] = []) {
    console.log('Number of note children:', children.length); // Log the number of note children
    const contentsArray = this.fb.array([]);
    this.metadata = [];
    this.originalContent = [];
    const sortedChildren = [...children].sort((a, b) => a.position - b.position);
    sortedChildren.forEach((child, index) => {
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
    this.setFormValueChanges(); // Reset value changes detection after loading children
  }

  setFormValueChanges() {
    if (!this.noteForm) return; // Ensure form is initialized

    this.noteForm.get('title')?.valueChanges.pipe(debounceTime(300)).subscribe(() => {
      if (!this.initialLoad) {
        console.log('Title changed:', this.noteForm.value.title);
        this.handleTitleChange();
      }
    });

    this.contents.controls.forEach((control, index) => {
      control.valueChanges.pipe(debounceTime(300)).subscribe(() => {
        if (!this.initialLoad) {
          console.log(`Content at index ${index} changed:`, control.value);
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
    } else if (
      (event.key === 'Backspace' || event.key === 'Delete') &&
      contentControl.value.trim() === ''
    ) {
      event.preventDefault();
      this.deleteContentField(contentIndex);
    } else if (contentControl.value === '' && event.key === '/') {
      this.showDropdown = true;
      this.highlightedIndex = 0;
    } else if (this.showDropdown) {
      if (event.key === 'Enter') {
        event.preventDefault();
        this.selectDropdownOption(this.dropdownOptions[this.highlightedIndex], contentIndex);
      } else if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.highlightedIndex = (this.highlightedIndex + 1) % this.dropdownOptions.length;
      } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        this.highlightedIndex = (this.highlightedIndex - 1 + this.dropdownOptions.length) % this.dropdownOptions.length;
      }
    }
  }

  addContentField(position: number) {
    const control = this.fb.control('');
    this.contents.insert(position, control);
    this.metadata.splice(position, 0, { childId: null });
    this.originalContent.splice(position, 0, '');
    this.notesUpdated.emit();
    this.setFormValueChanges(); // Ensure value changes are detected for the new control

    setTimeout(() => {
      this.noteContentElements.toArray()[position].nativeElement.focus();
    }, 0);
  }

  deleteContentField(contentIndex: number) {
    const noteId = this.selectedNoteId;
    const textEntryId = this.metadata[contentIndex].childId;

    if (noteId && textEntryId) {
      this.noteService.deleteTextEntry(noteId, textEntryId).subscribe(() => {
        this.contents.removeAt(contentIndex);
        this.metadata.splice(contentIndex, 1);
        this.originalContent.splice(contentIndex, 1);
        this.notesUpdated.emit();
      }, (error: any) => {
        console.error('Error deleting text entry:', error);
      });
    } else {
      this.contents.removeAt(contentIndex);
      this.metadata.splice(contentIndex, 1);
      this.originalContent.splice(contentIndex, 1);
    }
  }

  handleTitleChange() {
    if (this.noteForm.value.title !== this.originalTitle) {
      console.log('Saving title:', this.noteForm.value.title);
      this.saveNoteTitle();
    }
  }

  handleTextChange(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const newText = contentControl.value.trim();
    if (newText !== this.originalContent[contentIndex]) {
      console.log(`Content changed at index ${contentIndex}:`, newText);
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
        this.notesUpdated.emit();
        this.originalContent[contentIndex] = newText; // Update the original content after successful save
        console.log(`Text entry created at index ${contentIndex} with ID ${response.id}`);
      }, (error: any) => {
        console.error('Error creating text entry:', error);
      });
    }
  }

  saveTextEntry(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const noteId = this.selectedNoteId;
    const textEntryId = this.metadata[contentIndex].childId;
    const newText = contentControl.value.trim();

    console.log(`Attempting to save text entry at index ${contentIndex}:`, { noteId, textEntryId, newText });

    if (noteId && textEntryId) {
      this.noteService.editTextEntry(noteId, textEntryId, newText, contentIndex).subscribe(() => {
        this.notesUpdated.emit();
        this.originalContent[contentIndex] = newText; // Update the original content after successful save
        console.log(`Text entry at index ${contentIndex} saved successfully`);
      }, (error: any) => {
        console.error('Error editing text entry:', error);
      });
    }
  }

  saveNoteTitle() {
    if (this.selectedNoteId === null) return;

    this.noteService.editNoteTitle(this.selectedNoteId, this.noteForm.value.title).subscribe(() => {
      this.notesUpdated.emit();
      this.originalTitle = this.noteForm.value.title; // Update the original title after successful save
    }, (error: any) => {
      console.error('Error editing note title:', error);
    });
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
          this.createTextEntry(index);
        }
      });
    }
  }

  selectDropdownOption(option: string, contentIndex: number) {
    if (option === '/image') {
      // Handle image upload here
      console.log('Selected /image option');
      this.showImageUpload(contentIndex);
    }
    this.showDropdown = false;
  }

  showImageUpload(contentIndex: number) {
    // Implement your image upload logic here
    // You can use a file input element and trigger click programmatically
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        // Handle file upload
        this.uploadImage(file, contentIndex);
      }
    };
    input.click();
  }

  uploadImage(file: File, contentIndex: number) {
    const noteId = this.selectedNoteId;
    if (noteId) {
      this.noteService.uploadImage(noteId, file, contentIndex).subscribe((response: any) => {
        // Handle response and update the note content with the image node
        console.log('Image uploaded successfully:', response);
        // Add logic to update the note content with the new image node
      }, (error: any) => {
        console.error('Error uploading image:', error);
      });
    }
  }
}
