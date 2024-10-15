import { Component, Input, Output, EventEmitter, OnInit, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { NoteChild, TextNode } from '../../../../models/note.model';
import { ImageUploadEvent } from '../../../../models/image.upload.event';

@Component({
  selector: 'app-text-child',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './text-child.component.html',
  styleUrls: ['./text-child.component.css'],
})
export class TextChildComponent implements OnInit {
  
  @Input() textChild!: NoteChild;
  @Input() noteChildren!: NoteChild[];  // Pass noteChildren array from MainSectionComponent
  @Input() deletePreviousImage!: (id: string) => void;  // Pass the delete function from MainSectionComponent
  @Output() textUpdated = new EventEmitter<NoteChild>();
  @Output() textDeleted = new EventEmitter<string>();
  @Output() triggerImageUpload = new EventEmitter<ImageUploadEvent>();
  @Output() createNewTextChild = new EventEmitter<void>();
  @Input() focusOnPreviousTextChild!: (position: number) => void;  // Input for focusing on previous text node
  @Input() getTextChildComponentByPosition!: (position: number) => TextChildComponent | null;
  @Output() focusOnPreviousNode = new EventEmitter<string>();
  @Output() deleteNode = new EventEmitter<string>();
  @Output() requestFocusOrDeletePreviousNode = new EventEmitter<void>();

  @ViewChild('textareaElement') textareaElement!: ElementRef;

  textForm: FormGroup;
  showDropdown: boolean = false;

  constructor(private fb: FormBuilder) {
    this.textForm = this.fb.group({
      content: [''],
    });
  }

  ngOnInit() {
    if (this.textChild?.textNode) {
      this.textForm.patchValue({ content: this.textChild.textNode.content });
    }

    this.getFormControl('content')?.valueChanges.subscribe((newContent) => {
      if (this.textChild?.textNode) {
        const updatedTextNode: TextNode = {
          ...this.textChild.textNode,
          content: newContent,
        };

        this.textUpdated.emit({ ...this.textChild, textNode: updatedTextNode });

        if (newContent.includes('/')) {
          this.showDropdown = true;
        } else {
          this.showDropdown = false;
        }
      }
    });
  }

  getFormControl(controlName: string): FormControl {
    return this.textForm.get(controlName) as FormControl;
  }

  focusTextArea(position: 'start' | 'end' = 'start') {
    setTimeout(() => {
      if (this.textareaElement && this.textareaElement.nativeElement) {
        const textarea = this.textareaElement.nativeElement;
        textarea.focus();

        if (position === 'end') {
          textarea.setSelectionRange(textarea.value.length, textarea.value.length);
        } else if (position === 'start') {
          textarea.setSelectionRange(0, 0);
        }

        console.log('Focusing textarea at position:', position, 'for textChild:', this.textChild.id);
      }
    }, 50);
  }

  @HostListener('keydown.backspace', ['$event'])
  handleBackspace(event: KeyboardEvent) {
    const cursorPosition = this.textareaElement.nativeElement.selectionStart;
  
    if (cursorPosition === 0) {
      event.preventDefault();
  
      // Check if the text content is empty and delete
      if (this.getFormControl('content').value === '') {
        // Emit the textDeleted event
        if (this.textChild.id) {
          this.textDeleted.emit(this.textChild.id);
        } else {
          console.error('Cannot emit textDeleted event, textChild.id is null or undefined.');
        }
  
        // Request parent to focus or delete the previous node
        this.requestFocusOrDeletePreviousNode.emit();
      } else {
        // Request parent to focus on the previous text node
        this.requestFocusOrDeletePreviousNode.emit();
      }
    }
  }
  

  getPreviousChild() {
    const index = this.textChild.position;
    return index > 0 ? this.noteChildren[index - 1] : null;
  }
  
  jumpToEndOfPreviousTextarea() {
    if (this.textChild.position > 0) {
      console.log('Current position:', this.textChild.position);
  
      const previousTextChildComponent = this.getTextChildComponentByPosition(this.textChild.position - 1);
  
      if (previousTextChildComponent) {
        setTimeout(() => {
          previousTextChildComponent.focusTextArea('end');
        }, 0); // Ensure DOM rendering is complete before focusing
      } else {
        console.error('ERROR: Could not move to previous text child at position:', this.textChild.position - 1);
      }
    } else {
      console.warn('No previous text node, already at the first node.');
    }
  }
  
  // Renamed this method to avoid conflict with @Input property
  handleDeletePreviousImage() {
    const previousChild = this.getPreviousChild();
    if (previousChild?.type === 'image') {
      this.deletePreviousImage(previousChild.id!);  // Use the @Input method to delete the image
    }
  }

  @HostListener('keydown.shift.enter', ['$event'])
  handleShiftEnter(event: KeyboardEvent) {
    event.preventDefault();
    this.createNewTextChild.emit();
  }

  triggerFileUpload() {
    const imageInput = document.createElement('input');
    imageInput.type = 'file';
    imageInput.accept = 'image/*';

    imageInput.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        this.triggerImageUpload.emit({ file, textChildId: this.textChild.id! });
      }
    };

    imageInput.click();
  }
}
