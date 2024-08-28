import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChildren, QueryList, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, FormControl } from '@angular/forms';
import { catchError, debounceTime, tap } from 'rxjs/operators';
import { NoteService } from '../../../services/note.service';
import { Note, NoteChild } from '../../../models/note.model';
import { Observable, of, throwError } from 'rxjs';

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

  dropdownVisible: boolean[] = [];
  filteredOptions: string[] = ['image'];
  highlightedIndex: number = 0;

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

      this.noteService.getNoteChildren(this.selectedNoteId!).subscribe({
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
        this.metadata.push({ childId: child.id });
        this.originalContent.push(child.textNode.content);
      } else if (child.type === 'image' && child.imageNode) {
        const imagePath = child.imageNode.imagePath;
  
        if (imagePath) {
          const control = this.fb.control('');
          contentsArray.push(control);
          this.metadata.push({ childId: child.id });
          this.originalContent.push('');
  
          setTimeout(() => {
            const textareaElement = this.noteContentElements.toArray()[index]?.nativeElement;
            if (textareaElement && textareaElement.parentNode) {
              const imageElement = document.createElement('img');
              imageElement.src = `${this.noteService.getImageUrl(imagePath)}`;
              imageElement.alt = 'Uploaded Image';
              imageElement.style.maxWidth = '100%';
  
              textareaElement.parentNode.replaceChild(imageElement, textareaElement);
            } else {
              console.error('Textarea element or its parent node is not available');
            }
          }, 0);
        } else {
          console.error('imagePath is undefined');
        }
      }
    });
  
    this.noteForm.setControl('contents', contentsArray);
    this.addEmptyContentField(contentsArray);
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
    const textareaElement = this.noteContentElements.toArray()[contentIndex]?.nativeElement;

    if (textareaElement && event.shiftKey && event.key === 'Enter') {
      event.preventDefault();

      if (this.isInEmptyTextarea(contentIndex)) {
        return;
      }

      if (this.isLastTextNode(contentIndex)) {
        this.moveToNextEmptyTextNode(contentIndex);
      } else {
        this.addContentField(contentIndex + 1);
      }
    } else if (textareaElement && event.key === 'Backspace') {
      if (textareaElement.selectionStart === 0 && contentControl.value.length === 0) {
        if (this.metadata[contentIndex].childId) {
          this.deleteContentField(contentIndex);
          setTimeout(() => this.moveToPreviousTextNode(contentIndex), 0);
        } else if (contentIndex > 0) {
          const previousChildId = this.metadata[contentIndex - 1]?.childId;
          const previousChildType = this.noteChildren.find(child => child.id === previousChildId)?.type;

          if (previousChildType === 'image') {
            this.deleteContentField(contentIndex - 1);
            setTimeout(() => this.moveToPreviousTextNode(contentIndex - 1), 0);
          } else {
            this.moveToPreviousTextNode(contentIndex);
          }
        }
        event.preventDefault();
      } else if (textareaElement.selectionStart === 0 && contentIndex > 0) {
        const previousControl = this.contents.at(contentIndex - 1) as FormControl;
        const previousChildId = this.metadata[contentIndex - 1]?.childId;

        if (previousChildId) {
          const previousChildType = this.noteChildren.find(child => child.id === previousChildId)?.type;

          if (previousChildType === 'text') {
            setTimeout(() => {
              const previousTextareaElement = this.noteContentElements.toArray()[contentIndex - 1]?.nativeElement;
              if (previousTextareaElement) {
                previousTextareaElement.focus();
                previousTextareaElement.setSelectionRange(previousControl.value.length, previousControl.value.length);
              }
            }, 0);
            event.preventDefault();
          }
        }
      }
    } else if (event.key === 'Delete' && contentControl.value.trim() === '') {
      event.preventDefault();
      this.deleteContentField(contentIndex);
    } else if (event.key === 'ArrowDown' && this.dropdownVisible[contentIndex]) {
      event.preventDefault();
      this.highlightedIndex = (this.highlightedIndex + 1) % this.filteredOptions.length;
    } else if (event.key === 'ArrowUp' && this.dropdownVisible[contentIndex]) {
      event.preventDefault();
      this.highlightedIndex = (this.highlightedIndex - 1 + this.filteredOptions.length) % this.filteredOptions.length;
    } else if (event.key === 'Enter' && this.dropdownVisible[contentIndex]) {
      event.preventDefault();
      this.handleOptionClick(contentIndex, this.filteredOptions[this.highlightedIndex]);
    }
  }

  isLastTextNode(contentIndex: number): boolean {
    return (
      contentIndex === this.contents.length - 2 &&
      this.metadata[contentIndex].childId !== null &&
      this.metadata[contentIndex + 1].childId === null
    );
  }

  isInEmptyTextarea(contentIndex: number): boolean {
    return (
      contentIndex === this.contents.length - 1 &&
      this.metadata[contentIndex].childId === null &&
      this.contents.at(contentIndex).value === ''
    );
  }

  moveToNextEmptyTextNode(contentIndex: number) {
    if (contentIndex < this.contents.length - 1) {
      setTimeout(() => {
        const nextTextareaElement = this.noteContentElements.toArray()[contentIndex + 1]?.nativeElement;
        if (nextTextareaElement) {
          nextTextareaElement.focus();
          nextTextareaElement.setSelectionRange(0, 0);
        }
      }, 0);
    }
  }

  moveToPreviousTextNode(contentIndex: number) {
    if (contentIndex > 0) {
      const previousControl = this.contents.at(contentIndex - 1) as FormControl;
      const previousChildId = this.metadata[contentIndex - 1].childId;

      if (previousChildId) {
        const previousChildType = this.noteChildren.find(child => child.id === previousChildId)?.type;

        if (previousChildType === 'text') {
          const previousTextareaElement = this.noteContentElements.toArray()[contentIndex - 1].nativeElement;
          previousTextareaElement.focus();
          previousTextareaElement.setSelectionRange(previousControl.value.length, previousControl.value.length);
        }
      }
    }
  }

  handleInput(event: Event, contentIndex: number) {
    const input = (event.target as HTMLTextAreaElement).value;
    if (input === '/') {
      this.dropdownVisible[contentIndex] = true;
      this.highlightedIndex = 0;
    } else if (this.dropdownVisible[contentIndex]) {
      this.filteredOptions = this.filteredOptions.filter(option =>
        option.toLowerCase().includes(input.substring(1).toLowerCase())
      );
      if (this.filteredOptions.length === 0) {
        this.dropdownVisible[contentIndex] = false;
      }
    } else {
      this.dropdownVisible[contentIndex] = false;
    }
  }

  handleOptionClick(contentIndex: number, option: string) {
    if (option === 'image') {
      const noteChildId = this.metadata[contentIndex]?.childId;
      if (noteChildId) {
        this.uploadImage(noteChildId);
      } else {
        this.createImageEntry(contentIndex);
      }
    }
    this.dropdownVisible[contentIndex] = false;
  }

  uploadImage(noteChildId: string) {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    fileInput.onchange = () => {
      const file = fileInput.files ? fileInput.files[0] : null;
      if (file && noteChildId) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('noteId', this.selectedNoteId!);
        formData.append('noteChildId', noteChildId);
  
        this.noteService.uploadImage(formData).subscribe({
          next: (response: any) => {
            const imagePath = response.imagePath; // Make sure imagePath is correctly assigned
            this.reloadNoteChildren(this.selectedNoteId!).subscribe(() => {
              setTimeout(() => {
                const index = this.metadata.findIndex(meta => meta.childId === noteChildId);
                if (index !== -1) {
                  const textareaElement = this.noteContentElements.toArray()[index]?.nativeElement;
                  if (textareaElement && textareaElement.parentNode) {
                    const imageElement = document.createElement('img');
                    imageElement.src = `${this.noteService.getImageUrl(imagePath)}`; // Use imagePath here
                    imageElement.alt = 'Uploaded Image';
                    imageElement.style.maxWidth = '100%';
  
                    textareaElement.parentNode.replaceChild(imageElement, textareaElement);
                  }
                }
              }, 0);
            });
          },
          error: (error: any) => console.error('Error uploading image:', error)
        });
      }
    };
    fileInput.click();
  }
  
  
  


  createImageEntry(contentIndex: number) {
    const noteId = this.selectedNoteId;
    const control = this.contents.at(contentIndex) as FormControl;

    if (noteId && control.value.trim() === '') {
      this.uploadImage(this.metadata[contentIndex].childId || '');
    }
  }

  replaceTextNodeWithImage(noteChildId: string, imagePath: string) {
    const contentIndex = this.metadata.findIndex(meta => meta.childId === noteChildId);
    if (contentIndex !== -1) {
      this.contents.removeAt(contentIndex);
      this.metadata.splice(contentIndex, 1);
      this.originalContent.splice(contentIndex, 1);

      const imageControl = this.fb.control('');
      this.contents.insert(contentIndex, imageControl);

      setTimeout(() => {
        const textareaElement = this.noteContentElements.toArray()[contentIndex].nativeElement;
        const imageElement = document.createElement('img');
        imageElement.src = `${this.noteService.getImageUrl(imagePath)}`;
        imageElement.alt = 'Uploaded Image';
        imageElement.style.maxWidth = '100%';

        textareaElement.parentNode.replaceChild(imageElement, textareaElement);
      }, 0);
    }
  }

  handleTextChange(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const newText = contentControl.value.trim();
  
    if (newText !== this.originalContent[contentIndex]) {
      if (!this.metadata[contentIndex].childId) {
        this.createTextEntry(contentIndex).subscribe((response: any) => {
          if (response) {
            this.metadata[contentIndex].childId = response.id;
            this.originalContent[contentIndex] = newText;
  
            this.reloadNoteChildren(this.selectedNoteId!).subscribe(() => {
              setTimeout(() => {
                this.noteContentElements.toArray()[contentIndex].nativeElement.focus();
              }, 0);
            });
          }
        });
      } else {
        this.saveTextEntry(contentIndex);
        this.reloadNoteChildren(this.selectedNoteId!); // Add this line to reload children after saving the text entry
      }
    }
  }
  

  createTextEntry(contentIndex: number): Observable<any> {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const noteId = this.selectedNoteId;
    const newText = contentControl.value.trim();

    if (noteId && newText !== '') {
      return this.noteService.createTextEntry(noteId, newText, contentIndex).pipe(
        tap((response: any) => {
          this.metadata[contentIndex].childId = response.id;
          this.originalContent[contentIndex] = newText;
        }),
        catchError(error => {
          console.error('Error creating text entry:', error);
          return throwError(error);
        })
      );
    }

    return of(null);
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

  reloadNoteChildren(noteId: string): Observable<any> {
    return this.noteService.getNoteChildren(noteId).pipe(
      tap({
        next: (response: any) => {
          this.noteChildren = response.data;
          this.loadNoteChildren(this.noteChildren);
        },
        error: (error: any) => console.error('Error reloading note children:', error)
      })
    );
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
    const noteChildId = this.metadata[contentIndex].childId;

    if (noteId && noteChildId) {
      this.noteService.deleteTextEntry(noteId, noteChildId).subscribe(() => {
        this.contents.removeAt(contentIndex);
        this.metadata.splice(contentIndex, 1);
        this.originalContent.splice(contentIndex, 1);
        this.addEmptyContentFieldIfNecessary();
        this.notesUpdated.emit();
      }, (error: any) => console.error('Error deleting text entry:', error));
    } else {
      this.contents.removeAt(contentIndex);
      this.metadata.splice(contentIndex, 1);
      this.originalContent.splice(contentIndex, 1);
      this.addEmptyContentFieldIfNecessary();
    }
  }

  addEmptyContentFieldIfNecessary() {
    if (this.contents.length === 0 || this.metadata[this.metadata.length - 1].childId !== null) {
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
