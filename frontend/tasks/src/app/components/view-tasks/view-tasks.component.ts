import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Inject,
  LOCALE_ID,
  OnInit,
  ViewChild
} from '@angular/core';
import {AsyncPipe, CommonModule, formatDate, FormatWidth, getLocaleDateFormat, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {MatButton, MatIconButton} from "@angular/material/button";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow, MatRowDef, MatTable, MatTableModule
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import {MatLabel} from "@angular/material/form-field";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSort, MatSortModule} from "@angular/material/sort";
import {HttpClientModule} from "@angular/common/http";
import {MatInputModule} from "@angular/material/input";
import {RouterModule} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {ToastrService} from "ngx-toastr";
import {TaskService} from "../../service/task.service";
import {catchError, map, merge, of, tap} from "rxjs";
import {Page} from "../../domain/generic";
import {TasksDataSource} from "./tasks-data-source";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {TaskCategory} from "../../domain/task-category";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {EditTaskCategoryComponent} from "../edit-task-category/edit-task-category.component";
import {AddTaskComponent} from "../add-task/add-task.component";
import {Task} from "../../domain/task";

@Component({
  selector: 'app-view-tasks',
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
    MatIcon,
    MatDatepicker,
    MatDatepickerToggle,
    MatDatepickerInput,
    MatOption,
    MatSelect
  ],
  templateUrl: './view-tasks.component.html',
  styleUrl: './view-tasks.component.css'
})
export class ViewTasksComponent implements AfterViewInit, OnInit {
  displayedColumns = ["id", "name", "description",  "deadline", "category", "action"];
  dataSource : TasksDataSource
  totalElements : number = 0
  defaultPage : number = 0
  defaultSize : number = 5
  defaultSort : string = "DESC"
  taskName : string
  categoryName: string
  deadline: Date;
  deadlineMode: string = 'AFTER';
  isSubmitted: boolean = false;
  deadlineModes = ['AFTER', 'BEFORE'];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild('nameInput') input: ElementRef;

  constructor(private taskService : TaskService, private taskCategoryService : TaskCategoriesService, private changeDetector: ChangeDetectorRef, private dialog : MatDialog, private toastr: ToastrService, @Inject( LOCALE_ID ) private locale: string) {}

  ngOnInit(): void {
    this.dataSource = new TasksDataSource(this.taskService);
    this.taskService.get(this.defaultSize, this.defaultPage, this.defaultSort, this.deadlineMode).pipe(
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
        tap(() => this.loadTasksPage())
      )
      .subscribe();

    this.loadTasksPage()
    this.changeDetector.detectChanges();
  }

  onSubmit() { this.isSubmitted = true; }

  loadTasksPage() {
    let categoryId : number = 0
    if(this.categoryName != null && this.categoryName != '') {
      this.taskCategoryService.get(1, 0, 'ASC', this.categoryName).subscribe(
        value => {
          if(value.elements != null && value.totalElements != 0) {
            categoryId = value.elements[0].id
          }

          this.load(this.paginator.pageSize,
            this.paginator.pageIndex,
            this.sort.direction,
            this.deadlineMode,
            this.taskName,
            categoryId,
            this.deadline)
        },
      )
    } else {
      console.log("but i am here")
      this.load(this.paginator.pageSize,
        this.paginator.pageIndex,
        this.sort.direction,
        this.deadlineMode,
        this.taskName,
        undefined,
        this.deadline)
    }
  }

  load(pageSize: number, pageNumber: number, sortDirection: string, deadlineMode: string, name?: string, categoryId?: number, deadline?: Date) {
    console.log("Here i have some taskCategory id " + categoryId)
    this.dataSource.loadTasks(
      pageSize,
      pageNumber,
      sortDirection,
      deadlineMode,
      name,
      categoryId,
      deadline
    );
  }

  deleteTask(id : number) {
    this.taskService.delete(id).subscribe({
      next: () => {
        alert("Task Deleted")
        window.location.reload();
        //todo change into toaster later
      }
    })
  }

  openAddTaskForm() {
    this.dialog.open(AddTaskComponent)
  }

}
