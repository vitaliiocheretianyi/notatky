import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NoteChild } from '../../../../models/note.model';

@Component({
  selector: 'app-image-child',
  standalone: true,
  templateUrl: './image-child.component.html',
  styleUrls: ['./image-child.component.css'],
})
export class ImageChildComponent {
  @Input() imageChild!: NoteChild;
  @Output() imageDeleted = new EventEmitter<string>();

  // Your backend API URL
  private backendUrl = 'http://localhost:8080/notatky/note-children/images';

  // Generate the image URL
  getImageUrl(imageNodeId: string | null | undefined): string {
    if (!imageNodeId) {
      return ''; // Return an empty string or a placeholder image path if the imageNodeId is null or undefined
    }
    return `${this.backendUrl}/${imageNodeId}`;
  }
  

  deleteImage() {
    if (this.imageChild.id != null) {
      this.imageDeleted.emit(this.imageChild.id);
    }
  }
}
