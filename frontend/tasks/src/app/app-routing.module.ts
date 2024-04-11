import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {AddTaskCategoryComponent} from "./components/add-task-category/add-task-category.component";

const routes: Routes = [];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
