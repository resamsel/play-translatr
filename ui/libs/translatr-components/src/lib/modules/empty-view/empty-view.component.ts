import { Component, Input } from '@angular/core';

@Component({
  selector: 'dev-empty-view',
  templateUrl: './empty-view.component.html',
  styleUrls: ['./empty-view.component.scss']
})
export class EmptyViewComponent {
  @Input() icon: string;
  @Input() justifyContent: 'start' | 'center' = 'start';
  @Input() alignment: 'horizontal' | 'vertical' = 'horizontal';
}