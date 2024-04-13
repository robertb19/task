import {
  AfterContentChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnInit,
  ViewChild
} from '@angular/core';
import {CommonModule, NgFor} from "@angular/common";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {MatPaginator, MatPaginatorModule, PageEvent} from "@angular/material/paginator";
import {TaskCategory} from "../../domain/task-category";
import {MatSort, MatSortModule, Sort} from '@angular/material/sort';
import {
  MatCell,
  MatColumnDef,
  MatHeaderCell,
  MatTableModule
} from '@angular/material/table';
import {HttpClientModule} from "@angular/common/http";
import {MatInputModule} from "@angular/material/input";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {TaskCategoriesDataSource} from "./categories-data-source";
import {catchError, debounceTime, distinctUntilChanged, finalize, fromEvent, map, merge, of, tap} from "rxjs";
import {ActivatedRoute, RouterModule} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {AddTaskCategoryComponent} from "../add-task-category/add-task-category.component";
import {MatIcon} from "@angular/material/icon";
import {ToastrService} from "ngx-toastr";
import {EditTaskCategoryComponent} from "../edit-task-category/edit-task-category.component";
import {Page} from "../../domain/generic";

@Component({
  selector: 'app-view-task-categories',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginator,
    MatCell,
    MatHeaderCell,
    MatColumnDef,
    HttpClientModule,
    MatInputModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressSpinnerModule,
    RouterModule,
    FormsModule,
    MatButton,
    MatIconButton,
    MatIcon
  ],
  templateUrl: './view-task-categories.component.html',
  styleUrl: './view-task-categories.component.css'
})
export class ViewTaskCategoriesComponent implements AfterViewInit, OnInit {
  displayedColumns = ["id", "name", "description", "action"];
  dataSource : TaskCategoriesDataSource
  totalElements : number = 0
  defaultPage : number = 0
  defaultSize : number = 5
  defaultSort : string = "DESC"
  categoryName : string
  isSubmitted: boolean = false;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild('nameInput') input: ElementRef;

  constructor(private taskCategoryService : TaskCategoriesService, private changeDetector: ChangeDetectorRef, private dialog : MatDialog, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.dataSource = new TaskCategoriesDataSource(this.taskCategoryService);
    this.taskCategoryService.get(this.defaultSize, this.defaultPage, this.defaultSort).pipe(
      map((response) => response),
      catchError(() => of([])),
    )
      .subscribe(page => {
        let mappedPage = page as Page
        this.totalElements = mappedPage.totalElements
      });
  }

  ngAfterViewInit() {
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);

    merge(this.sort.sortChange, this.paginator.page)
      .pipe(
        tap(() => this.loadTaskCategoriesPage())
      )
      .subscribe();

    this.loadTaskCategoriesPage()
    this.changeDetector.detectChanges();
  }

  onSubmit() { this.isSubmitted = true; }

  loadTaskCategoriesPage() {
    console.log(this.categoryName)
    this.dataSource.loadTaskCategories(
      this.paginator.pageSize,
      this.paginator.pageIndex,
      this.sort.direction,
      this.categoryName
      );
  }

  openAddCategoryForm() {
    console.log("I work")
    this.dialog.open(AddTaskCategoryComponent)
  }

  openEditCategoryForm(data : TaskCategory) {
    this.dialog.open(EditTaskCategoryComponent, {data: data})
  }

  deleteTaskCategory(id : number) {
    this.taskCategoryService.delete(id).subscribe({
      next: () => {
        alert("Task Category Deleted")
        window.location.reload();
        //todo change into toaster later
      }
    })
  }
}
