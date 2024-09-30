// text-child.component.ts
import { Component, Input, Output, EventEmitter, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { NoteChild, TextNode } from '../../../../models/note.model';

@Component({
  selector: 'app-text-child',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './text-child.component.html',
  styleUrls: ['./text-child.component.css'],
})
export class TextChildComponent implements OnInit {
  @Input() textChild!: NoteChild;
  @Output() textUpdated = new EventEmitter<NoteChild>();
  @Output() textDeleted = new EventEmitter<string>();

  textForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.textForm = this.fb.group({
      content: [''],
    });
  }

  // text-child.component.ts
  ngOnInit() {
    if (this.textChild?.textNode) {
      console.log("TextChildComponent initialized with:", this.textChild);
      this.textForm.patchValue({ content: this.textChild.textNode.content });
    }

    this.getFormControl('content')?.valueChanges.subscribe((newContent) => {
      if (this.textChild?.textNode) {
        // Emit the updated content
        const updatedTextNode: TextNode = {
          ...this.textChild.textNode,
          content: newContent,
        };

        // Emit the updated NoteChild, including the updated textNode
        this.textUpdated.emit({ ...this.textChild, textNode: updatedTextNode });
      }
    });
  }

  
  getFormControl(controlName: string): FormControl {
    // Ensure the control is cast as FormControl, handling null safety with the fallback
    return this.textForm.get(controlName) as FormControl;
  }

  // HostListener to detect backspace key press
  @HostListener('keydown.backspace', ['$event'])
  handleBackspace(event: KeyboardEvent) {
    // If the textarea is empty, delete the text node
    if (this.getFormControl('content').value === '') {
      event.preventDefault();
      this.textDeleted.emit(this.textChild.id!);
    }
  }
}
