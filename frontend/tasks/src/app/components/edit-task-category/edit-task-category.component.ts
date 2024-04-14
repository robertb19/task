import {Component, Inject, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {ToastrService} from 'ngx-toastr';
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {DialogRef} from "@angular/cdk/dialog";
import {EditTaskCategoryForm, TaskCategory} from "../../domain/task-category";
import {MatError, MatHint} from "@angular/material/form-field";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-edit-task-category',
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
  templateUrl: './edit-task-category.component.html',
  styleUrl: './edit-task-category.component.css'
})
export class EditTaskCategoryComponent implements OnInit {
  editTaskCategory: EditTaskCategoryForm = new EditTaskCategoryForm(undefined, undefined);

  isSubmitted: boolean = false;

  constructor(private router: Router, private taskCategoryService: TaskCategoriesService, private toastr: ToastrService, private dialog: DialogRef, @Inject(MAT_DIALOG_DATA) public data: TaskCategory) {
  }

  ngOnInit() {
    console.log(this.data);
    this.editTaskCategory.name = this.data.name;
    this.editTaskCategory.description = this.data.description;
  }

  EditTaskCategory() {
    this.isSubmitted = true;
    this.taskCategoryService.update(this.editTaskCategory, this.data.id).subscribe(async data => {
        if (data != null) {
          this.toastr.success("Successfully edited task category",'',  {timeOut: 5000});
          this.dialog.close()
          setTimeout(() => {
            this.dialog.close()
            window.location.reload();
          }, 1800);
        }
      },
      async error => {
        setTimeout(() => {
          this.toastr.error(error.message, '',  {timeOut: 3000})
          this.dialog.close()
        }, 500);
      });
  }

  onSubmit() {
    console.log("I submitted")
    this.isSubmitted = true;
  }
}
