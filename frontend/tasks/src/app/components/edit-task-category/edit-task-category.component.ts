import {Component, Inject, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {ToastrService} from 'ngx-toastr';
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {DialogRef} from "@angular/cdk/dialog";
import {EditTaskCategoryForm, TaskCategory} from "../../domain/task-category";

@Component({
  selector: 'app-edit-task-category',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatDialogActions,
    MatDialogClose
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
          this.toastr.success("Yay we succeeded");
          this.dialog.close()
          window.location.reload();
          setTimeout(() => {
            this.router.navigate(['/categories']);
          }, 500);
        }
      },
      async error => {
        this.toastr.error(error.message);
        window.location.reload();
        setTimeout(() => {
          this.router.navigate(['/categories']);
        }, 500);
      });
  }

  onSubmit() {
    console.log("I submitted")
    this.isSubmitted = true;
  }
}
