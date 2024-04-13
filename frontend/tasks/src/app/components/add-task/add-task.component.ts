import { Component } from '@angular/core';
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {AddTaskForm, AddTaskViewForm} from "../../domain/task";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {DialogRef} from "@angular/cdk/dialog";
import {TaskService} from "../../service/task.service";
import {AddTaskCategoryComponent} from "../add-task-category/add-task-category.component";
import {DateFilterFn, MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {MatLabel} from "@angular/material/form-field";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {MatNativeDateModule} from "@angular/material/core";
import {MatInput} from "@angular/material/input";

@Component({
  selector: 'app-add-task',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatDialogActions,
    MatDialogClose,
    MatDatepicker,
    MatDatepickerToggle,
    MatLabel,
    MatDatepickerInput,
    MatNativeDateModule,
    MatInput
  ],
  templateUrl: './add-task.component.html',
  styleUrl: './add-task.component.css'
})
export class AddTaskComponent {
  dateFilter(date: Date | null): boolean  {
    const today = new Date()
    if(date == null) return false
    return date >= today
  }

  addTask: AddTaskViewForm = new AddTaskViewForm("", "", new Date(), "")

  isSubmitted: boolean = false;

  constructor(private router: Router, private taskService: TaskService, private taskCategoryService: TaskCategoriesService, private toastr: ToastrService, private dialog: DialogRef) {}

  AddTask() {
    this.isSubmitted = true;
    let categoryId : number = 0
    this.taskCategoryService.get(1, 0, 'ASC', this.addTask.category_name).subscribe(
      value => {
        if(value.elements != null && value.totalElements != 0) {
          categoryId = value.elements[0].id
        }

        let deadlineAsEpochSecond = this.addTask.deadline.getTime() / 1000;
        let addTaskForm = new AddTaskForm(this.addTask.name, this.addTask.description, deadlineAsEpochSecond, categoryId);
        this.taskService.addTask(addTaskForm).subscribe(async data => {
            if (data != null) {
              var resultData = data;
              if (resultData != null) {
                console.log("HELOLLO")
                this.toastr.success(resultData.message);
                this.dialog.close()
                window.location.reload();
                setTimeout(() => {
                  this.router.navigate(['/tasks']);
                }, 500);
              }
            }
          },
          async error => {
            this.toastr.error(error.message);
            setTimeout(() => {
              this.router.navigate(['/tasks']);
            }, 500);
          });
      },
    )
  }

  onSubmit() {
    console.log("I submitted")
    this.isSubmitted = true;
  }

  protected readonly AddTaskCategoryComponent = AddTaskCategoryComponent;
}
