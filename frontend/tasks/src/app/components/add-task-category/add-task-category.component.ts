import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {ToastrService} from 'ngx-toastr';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-add-task-category',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './add-task-category.component.html',
  styleUrl: './add-task-category.component.css'
})
export class AddTaskCategoryComponent {
  addTaskCategory: AddTaskCategoryForm = new AddTaskCategoryForm("", "");

  isSubmitted: boolean = false;

  constructor(private router: Router, private taskCategoryService: TaskCategoriesService, private toastr: ToastrService) {
  }

  AddTaskCategory() {
    this.isSubmitted = true;
    this.taskCategoryService.addTaskCategory(this.addTaskCategory).subscribe(async data => {
        if (data != null && data.body != null) {
          var resultData = data.body;
          if (resultData != null && resultData.isSuccess) {
            this.toastr.success(resultData.message);
            setTimeout(() => {
              this.router.navigate(['/addTaskCategory']);
            }, 500);
          }
        }
      },
      async error => {
        this.toastr.error(error.message);
        setTimeout(() => {
          this.router.navigate(['/addTaskCategory']);
        }, 500);
      });
  }

  onSubmit() { this.isSubmitted = true; }
}

export class AddTaskCategoryForm {
  constructor(
    public name: string,
    public description: string
  ) {
  }
}
