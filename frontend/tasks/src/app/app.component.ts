import { Component } from '@angular/core';
import {Router, RouterLink, RouterOutlet} from '@angular/router';
import {AddTaskCategoryComponent} from "./components/add-task-category/add-task-category.component";
import {ViewTaskCategoriesComponent} from "./components/view-task-categories/view-task-categories.component";
import {MatSidenavContainer} from "@angular/material/sidenav";
import {MatToolbar, MatToolbarRow} from "@angular/material/toolbar";
import {SidebarComponent} from "./sidebar/sidebar.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AddTaskCategoryComponent, ViewTaskCategoriesComponent, MatSidenavContainer, MatToolbar, MatToolbarRow, RouterLink, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  constructor(private router: Router) { }

  HomeClick(){
    this.router.navigate(['AddTaskCategory']);
  }

  title = 'tasks';
}
