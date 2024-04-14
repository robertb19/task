import {Component, Inject, OnInit} from '@angular/core';
import {AddTaskForm, AddTaskViewForm, EditTaskForm, Task} from "../../domain/task";
import {Router} from "@angular/router";
import {TaskService} from "../../service/task.service";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {ToastrService} from "ngx-toastr";
import {DialogRef} from "@angular/cdk/dialog";
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {FormsModule} from "@angular/forms";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {MatError, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatButton} from "@angular/material/button";
import {MatInput} from "@angular/material/input";
import {DatePipe, NgIf} from "@angular/common";

@Component({
  selector: 'app-edit-task',
  standalone: true,
  imports: [
    FormsModule,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatError,
    MatDatepicker,
    MatHint,
    MatLabel,
    MatButton,
    MatDialogActions,
    MatSuffix,
    MatInput,
    MatDialogClose,
    NgIf
  ],
  templateUrl: './edit-task.component.html',
  styleUrl: './edit-task.component.css'
})
export class EditTaskComponent implements OnInit {
  dateFilter(date: Date | null): boolean  {
    const today = new Date()
    if(date == null) return false
    return date >= today
  }

  editTask: AddTaskViewForm = new AddTaskViewForm("", "", new Date(), "")

  isSubmitted: boolean = false;

  constructor(private router: Router, private taskService: TaskService, private taskCategoryService: TaskCategoriesService, private toastr: ToastrService, private dialog: DialogRef, @Inject(MAT_DIALOG_DATA) public data: Task) {}

  ngOnInit() {
    this.editTask.name = this.data.name;
    this.editTask.description = this.data.description;
    this.editTask.deadline = this.data.deadline;
    this.editTask.category_name = this.data.category.name;
  }

  EditTask() {
    this.isSubmitted = true;
    let categoryId : number = 0
    this.taskCategoryService.get(1, 0, 'ASC', this.editTask.category_name).subscribe(
      value => {
        if(value.elements != null && value.totalElements != 0) {
          categoryId = value.elements[0].id
        }

        let editTaskForm = new EditTaskForm(undefined, undefined, undefined, undefined);
        if(this.editTask.name != this.data.name) {
          editTaskForm.name = this.editTask.name
        }

        if(this.editTask.description != this.data.description) {
          editTaskForm.description = this.editTask.description
        }

        if(this.editTask.deadline != this.data.deadline) {
          editTaskForm.deadline = this.editTask.deadline.getTime() / 1000
        }

        if(this.editTask.category_name != this.data.category.name) {
          editTaskForm.categoryId = categoryId
        }

        this.taskService.update(editTaskForm, this.data.id).subscribe(async data => {
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
    this.isSubmitted = true;
  }
}
