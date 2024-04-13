import { Component } from '@angular/core';
import {FormControl, FormsModule, Validators} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {AddTaskForm, AddTaskViewForm} from "../../domain/task";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {DialogRef} from "@angular/cdk/dialog";
import {TaskService} from "../../service/task.service";
import {AddTaskCategoryComponent} from "../add-task-category/add-task-category.component";
import {DateFilterFn, MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {MatError, MatHint, MatLabel} from "@angular/material/form-field";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {MatNativeDateModule} from "@angular/material/core";
import {MatInput} from "@angular/material/input";
import {NgIf} from "@angular/common";

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
    MatInput,
    MatError,
    MatHint,
    NgIf
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
                this.toastr.success("Successfully added task",'',  {timeOut: 5000});
                this.dialog.close()
                setTimeout(() => {
                  this.dialog.close()
                  window.location.reload();
                }, 1800);
              }
            }
          },
          async error => {
            setTimeout(() => {
              this.toastr.error(error.message, '',  {timeOut: 3000})
              this.dialog.close()
            }, 500);
          });
      },
    )
  }

  onSubmit() {
    console.log("I submitted")
    this.isSubmitted = true;
  }
}
