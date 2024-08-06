import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dropdown',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dropdown" *ngIf="show">
      <div
        *ngFor="let option of options; let i = index"
        [class.highlighted]="i === highlightedIndex"
        (click)="selectOption(option)"
      >
        {{ option }}
      </div>
    </div>
  `,
  styles: [
    `
      .dropdown {
        border: 1px solid #ccc;
        background: #fff;
        position: absolute;
      }
      .dropdown div {
        padding: 8px;
        cursor: pointer;
      }
      .dropdown div.highlighted {
        background: #007bff;
        color: #fff;
      }
    `,
  ],
})
export class DropdownComponent {
  @Input() options: string[] = [];
  @Input() show: boolean = false;
  @Input() highlightedIndex: number = 0;
  @Output() optionSelected = new EventEmitter<string>();

  selectOption(option: string) {
    this.optionSelected.emit(option);
  }
}
