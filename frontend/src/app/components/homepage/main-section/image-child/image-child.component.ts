// image-child.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteChild } from '../../../../models/note.model';

@Component({
  selector: 'app-image-child',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './image-child.component.html',
  styleUrls: ['./image-child.component.css']
})
export class ImageChildComponent {
  @Input() imageChild!: NoteChild;
  @Output() imageDeleted = new EventEmitter<string>();

  deleteImageChild() {
    // Ensure that id is not null before emitting; use non-null assertion
    if (this.imageChild.id) {
      this.imageDeleted.emit(this.imageChild.id);
    } else {
      console.error('Cannot delete an image with null id.');
    }
  }
}
