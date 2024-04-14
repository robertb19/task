import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {ToastContainerDirective, ToastrService} from 'ngx-toastr';
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {DialogRef} from "@angular/cdk/dialog";
import {AddTaskCategoryForm} from "../../domain/task-category";
import {MatError, MatHint} from "@angular/material/form-field";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-add-task-category',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatDialogActions,
    MatDialogClose,
    MatError,
    MatHint,
    NgIf
  ],
  templateUrl: './add-task-category.component.html',
  styleUrl: './add-task-category.component.css'
})
export class AddTaskCategoryComponent implements OnInit{
  addTaskCategory: AddTaskCategoryForm = new AddTaskCategoryForm("", "");

  isSubmitted: boolean = false;

  @ViewChild(ToastContainerDirective, {static: true}) toastContainer: ToastContainerDirective;

  constructor(private router: Router, private taskCategoryService: TaskCategoriesService, private toastr: ToastrService,  private dialog: DialogRef) {}

  ngOnInit() {
    this.toastr.overlayContainer = this.toastContainer;
  }

  AddTaskCategory() {
    this.isSubmitted = true;
    this.taskCategoryService.addTaskCategory(this.addTaskCategory).subscribe({
    next: (data) => {
        if (data != null) {
          var resultData = data;
          if (resultData != null) {
            this.toastr.success("Successfully added task category",'',  {timeOut: 5000});
            this.dialog.close()
            setTimeout(() => {
              this.dialog.close()
              window.location.reload();
            }, 1800);
          }
        }
      },
      error: (error) => {
        setTimeout(() => {
          this.toastr.error(error.message, '',  {timeOut: 3000})
          this.dialog.close()
        }, 500);
      }});
  }

  onSubmit() {
    this.isSubmitted = true;
  }
}
