import {Component} from '@angular/core';
import {Router} from "@angular/router";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {ToastrService} from 'ngx-toastr';
import {FormsModule} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {MatDialogActions, MatDialogClose} from "@angular/material/dialog";
import {DialogRef} from "@angular/cdk/dialog";
import {AddTaskCategoryForm} from "../../domain/task-category";

@Component({
  selector: 'app-add-task-category',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatDialogActions,
    MatDialogClose
  ],
  templateUrl: './add-task-category.component.html',
  styleUrl: './add-task-category.component.css'
})
export class AddTaskCategoryComponent {
  addTaskCategory: AddTaskCategoryForm = new AddTaskCategoryForm("", "");

  isSubmitted: boolean = false;

  constructor(private router: Router, private taskCategoryService: TaskCategoriesService, private toastr: ToastrService, private dialog: DialogRef) {}

  AddTaskCategory() {
    this.isSubmitted = true;
    this.taskCategoryService.addTaskCategory(this.addTaskCategory).subscribe(async data => {
        if (data != null) {
          var resultData = data;
          if (resultData != null) {
            console.log("HELOLLO")
            this.toastr.success(resultData.message);
            this.dialog.close()
            window.location.reload();
            setTimeout(() => {
              this.router.navigate(['/categories']);
            }, 500);
          }
        }
      },
      async error => {
        this.toastr.error(error.message);
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
