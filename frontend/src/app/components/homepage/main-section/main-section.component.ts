import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChildren, QueryList, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, FormControl } from '@angular/forms';
import { debounceTime } from 'rxjs/operators';
import { Note, NoteChild } from '../../../models/note.model';
import { NoteService } from '../../../services/note.service';

@Component({
  selector: 'app-main-section',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './main-section.component.html',
  styleUrls: ['./main-section.component.css'],
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
      this.loadNoteChildren(this.noteChildren);
      setTimeout(() => this.initialLoad = false, 0);
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
            }
          }, 0);
        }
      }
    });

    this.noteForm.setControl('contents', contentsArray);
    this.addEmptyContentField(contentsArray);
    this.setFormValueChanges();
  }

  getLastNonEmptyTextNodeIndex(): number {
    let lastNonEmptyIndex = -1;
  
    // Iterate over the contents to find the last non-empty text node
    this.contents.controls.forEach((control, index) => {
      if (control.value && control.value.trim() !== '') {
        lastNonEmptyIndex = index;
      }
    });
  
    return lastNonEmptyIndex;
  }
  

  setFormValueChanges() {
    if (!this.noteForm) return;

    this.noteForm.get('title')?.valueChanges.pipe(debounceTime(300)).subscribe((newTitle: string) => {
      if (!this.initialLoad) {
        this.updateLocalNoteTitle(newTitle);
      }
    });

    this.contents.controls.forEach((control, index) => {
      control.valueChanges.pipe(debounceTime(300)).subscribe(() => {
        if (!this.initialLoad) {
          this.updateLocalTextEntry(index);
        }
      });
    });
  }

  get contents() {
    return this.noteForm.get('contents') as FormArray;
  }

  updateLocalNoteTitle(newTitle: string) {
    const note = this.notes.find(n => n.id === this.selectedNoteId);
    if (note) {
      note.title = newTitle;
      console.log('Note title updated locally:', note.title);
      console.log('Current note children:', this.noteChildren);
      this.notesUpdated.emit();
    }
  }

  updateLocalTextEntry(contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
    const newText = contentControl.value.trim();

    if (newText !== this.originalContent[contentIndex]) {
      this.originalContent[contentIndex] = newText;

      const childId = this.metadata[contentIndex].childId;
      if (childId) {
        const noteChild = this.noteChildren.find(child => child.id === childId);
        if (noteChild && noteChild.textNode) {
          noteChild.textNode.content = newText;
          console.log('Text entry updated:', noteChild);
        }
      }

      console.log('Current note children:', this.noteChildren);
      this.notesUpdated.emit();
    }
  }

  handleInput(event: Event, contentIndex: number) {
    const input = (event.target as HTMLTextAreaElement).value;

    if (input === '/') {
      this.dropdownVisible[contentIndex] = true;
      this.highlightedIndex = 0;
    } else if (this.dropdownVisible[contentIndex]) {
      this.filteredOptions = ['image'].filter(option =>
        option.toLowerCase().includes(input.substring(1).toLowerCase())
      );

      if (this.filteredOptions.length === 0) {
        this.dropdownVisible[contentIndex] = false;
      } else {
        this.dropdownVisible[contentIndex] = true; // Ensure dropdown stays open with valid options
      }
    } else {
      this.dropdownVisible[contentIndex] = false;
    }
  }

  handleKeyDown(event: KeyboardEvent, contentIndex: number) {
    const contentControl = this.contents.at(contentIndex) as FormControl;
  
    // Handling Shift + Enter
    if (event.key === 'Enter' && event.shiftKey) {
      event.preventDefault();
  
      const lastContentIsEmpty = this.contents.length > 0 && this.metadata[this.contents.length - 1].childId === null;
  
      // If at the last non-empty text node and an empty textarea exists, move to it
      if (contentIndex === this.getLastNonEmptyTextNodeIndex() && lastContentIsEmpty) {
        setTimeout(() => {
          const existingEmptyTextarea = this.noteContentElements.toArray()[this.contents.length - 1]?.nativeElement;
          if (existingEmptyTextarea) {
            existingEmptyTextarea.focus();
            existingEmptyTextarea.setSelectionRange(0, 0); // Set cursor at the beginning of the empty textarea
          }
        }, 0);
      } else {
        // Otherwise, add a new textarea right after the current one
        this.addContentField(contentIndex + 1);
  
        setTimeout(() => {
          const newTextarea = this.noteContentElements.toArray()[contentIndex + 1]?.nativeElement;
          if (newTextarea) {
            newTextarea.focus(); // Move the focus to the new textarea
            newTextarea.setSelectionRange(0, 0); // Set cursor at the beginning of the new textarea
          }
        }, 0);
      }
  
      return;
    }
  
    const textareaElement = this.noteContentElements.toArray()[contentIndex]?.nativeElement;
  
    // Handling Backspace
    if (textareaElement && event.key === 'Backspace') {
      if (textareaElement.selectionStart === 0 && contentControl.value.length === 0) {
        if (contentIndex > 0) {
          const previousChildId = this.metadata[contentIndex - 1]?.childId;
          const previousChildType = this.noteChildren.find(child => child.id === previousChildId)?.type;
  
          if (previousChildType === 'image') {
            this.deleteContentField(contentIndex - 1);
            event.preventDefault();
          } else if (this.metadata[contentIndex].childId) {
            this.deleteContentField(contentIndex);
            event.preventDefault();
          } else {
            this.moveToPreviousTextNode(contentIndex);
          }
        }
      }
    }
  
    // Detect when typing in the last textarea and auto-create a new one
    const lastContentIndex = this.contents.length - 1;
    if (contentIndex === lastContentIndex && event.key !== 'Backspace' && event.key !== 'Enter') {
      const isLastTextareaEmpty = contentControl.value.trim() === '';
  
      if (!isLastTextareaEmpty) {
        // Convert the existing textarea to a new text node
        this.convertToTextNode(contentIndex);
  
        // Automatically create a new empty textarea
        this.addContentField(contentIndex + 1);
  
        // Ensure the cursor stays in the converted textarea
        setTimeout(() => {
          const convertedTextarea = this.noteContentElements.toArray()[contentIndex]?.nativeElement;
          if (convertedTextarea) {
            convertedTextarea.focus();
            convertedTextarea.setSelectionRange(contentControl.value.length, contentControl.value.length); // Place cursor at the end of the text
          }
        }, 0);
      }
    }
  }

  
convertToTextNode(contentIndex: number) {
  const control = this.contents.at(contentIndex) as FormControl;
  const newTextContent = control.value.trim();

  // Create the new text node with the position property
  const newTextNode: NoteChild = {
    type: 'text',
    textNode: {
      content: newTextContent,
      id: `text-node-${contentIndex}`,
    },
    id: `note-child-${contentIndex}`,
    position: this.noteChildren.length // Set the position to the current length of noteChildren array
  };

  // Update the metadata and noteChildren with the new text node
  this.metadata[contentIndex].childId = newTextNode.id;
  this.noteChildren.push(newTextNode);

  // Emit event to notify that notes were updated
  this.notesUpdated.emit();

  // Focus on the newly converted text node and keep the cursor there
  setTimeout(() => {
    const textareaElement = this.noteContentElements.toArray()[contentIndex]?.nativeElement;
    if (textareaElement) {
      textareaElement.focus();
      textareaElement.setSelectionRange(newTextContent.length, newTextContent.length); // Place cursor at the end of the text
    }
  }, 0);
}


  handleOptionClick(contentIndex: number, option: string) {
    if (option === 'image') {
      this.addImageNode(contentIndex);
    }
    this.dropdownVisible[contentIndex] = false;
  }

  isLastTextNode(contentIndex: number): boolean {
    return (
      contentIndex === this.contents.length - 2 &&
      this.metadata[contentIndex].childId !== null &&
      this.metadata[contentIndex + 1].childId === null
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
          const previousTextareaElement = this.noteContentElements.toArray()[contentIndex - 1]?.nativeElement;
          previousTextareaElement.focus();
          previousTextareaElement.setSelectionRange(previousControl.value.length, previousControl.value.length);
        }
      }
    }
  }

  deleteContentField(contentIndex: number) {
    const noteChildId = this.metadata[contentIndex].childId;
    if (noteChildId) {
      this.noteChildren = this.noteChildren.filter(child => child.id !== noteChildId);
      console.log('Content field deleted at index:', contentIndex);
    }

    this.contents.removeAt(contentIndex);
    this.metadata.splice(contentIndex, 1);
    this.originalContent.splice(contentIndex, 1);
    this.addEmptyContentFieldIfNecessary();

    // Keep the focus on the same textarea after deleting content
    setTimeout(() => {
      const focusElement = this.noteContentElements.toArray()[contentIndex]?.nativeElement;
      if (focusElement) {
        focusElement.focus();
        focusElement.setSelectionRange(0, 0);
      }
    }, 0);

    console.log('Current note children:', this.noteChildren);
    this.notesUpdated.emit();
  }

  addContentField(position: number) {
    // Check if the user is trying to add a new content field at the last position and there's already an empty textarea
    const isAtLastPosition = position === this.contents.length;
    const lastContentIsEmpty = this.contents.length > 0 && this.metadata[this.contents.length - 1].childId === null;
  
    // Allow adding content fields in between, only apply the empty textarea check if we're at the last position
    if (isAtLastPosition && lastContentIsEmpty) {
      // Jump to the existing empty textarea
      setTimeout(() => {
        const existingTextarea = this.noteContentElements.toArray()[this.contents.length - 1]?.nativeElement;
        if (existingTextarea) {
          existingTextarea.focus();
          existingTextarea.setSelectionRange(0, 0);
        }
      }, 0);
    } else {
      // Insert new text node regardless of position
      const control = this.fb.control('');
      this.contents.insert(position, control);
      this.metadata.splice(position, 0, { childId: null });
      this.originalContent.splice(position, 0, '');
      this.notesUpdated.emit();
  
      setTimeout(() => {
        const newTextarea = this.noteContentElements.toArray()[position]?.nativeElement;
        newTextarea.focus();
        newTextarea.setSelectionRange(0, 0); // Place cursor at the start of the new textarea
      }, 0);
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

  addImageNode(contentIndex: number) {
    const imageControl = this.fb.control('');
    this.contents.insert(contentIndex, imageControl);
    this.metadata.splice(contentIndex, 0, { childId: null });
    this.originalContent.splice(contentIndex, 0, '');
  
    setTimeout(() => {
      const textareaElement = this.noteContentElements.toArray()[contentIndex].nativeElement;
      const imageElement = document.createElement('img');
      const imagePath = 'path/to/image/from/backend'; // Replace this with the correct backend path
      imageElement.src = `${this.noteService.getImageUrl(imagePath)}`;
      imageElement.alt = 'Uploaded Image';
      imageElement.style.maxWidth = '100%';
  
      textareaElement.parentNode.replaceChild(imageElement, textareaElement);
  
      // Set the focus back to the next textarea after inserting the image
      if (this.noteContentElements.toArray()[contentIndex + 1]) {
        const nextTextareaElement = this.noteContentElements.toArray()[contentIndex + 1].nativeElement;
        nextTextareaElement.focus();
        nextTextareaElement.setSelectionRange(0, 0); // Place cursor at the start of the next textarea
      }
  
      this.notesUpdated.emit();
      console.log('Image node added at index', contentIndex);
      console.log('Current note children:', this.noteChildren);
    }, 0);
  }
}
