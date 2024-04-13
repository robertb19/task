import { Component, inject } from '@angular/core';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { map } from 'rxjs/operators';
import { AsyncPipe } from '@angular/common';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import {ViewTaskCategoriesComponent} from "../components/view-task-categories/view-task-categories.component";

@Component({
  selector: 'app-task-categories-dashboard',
  templateUrl: './task-categories-dashboard.component.html',
  styleUrl: './task-categories-dashboard.component.css',
  standalone: true,
  imports: [
    AsyncPipe,
    MatGridListModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    ViewTaskCategoriesComponent
  ]
})
export class TaskCategoriesDashboardComponent {
  private breakpointObserver = inject(BreakpointObserver);

  /** Based on the screen size, switch from standard to one column per row */
  cards = this.breakpointObserver.observe(Breakpoints.Handset).pipe(
    map(({ matches }) => {
      if (matches) {
        return [
          { title: 'My Categories', cols: 1, rows: 1 },
        ];
      }

      return [
        { title: 'My Categories', cols: 2, rows: 1 },
      ];
    })
  );
}
