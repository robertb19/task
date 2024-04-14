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
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {MatButton, MatIconButton} from "@angular/material/button";
import {
  MatCell,
  MatColumnDef,
  MatHeaderCell,
  MatTableModule
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
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
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {AddTaskComponent} from "../add-task/add-task.component";
import {Task} from "../../domain/task";
import {EditTaskComponent} from "../edit-task/edit-task.component";
import {CalendarModule} from "primeng/calendar";
import {NgxMaterialTimepickerComponent, NgxMaterialTimepickerModule} from "ngx-material-timepicker";

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
    MatSelect,
    CalendarModule,
    NgxMaterialTimepickerModule
  ],
  templateUrl: './view-tasks.component.html',
  styleUrl: './view-tasks.component.css'
})
export class ViewTasksComponent implements AfterViewInit, OnInit {
  displayedColumns = ["id", "name", "description", "deadline", "category", "action"];
  dataSource: TasksDataSource
  totalElements: number = 0
  defaultPage: number = 0
  defaultSize: number = 5
  defaultSort: string = "DESC"
  taskName: string
  categoryName: string
  deadline: Date
  time: string
  deadlineMode: string = 'AFTER';
  isSubmitted: boolean = false;
  deadlineModes = ['AFTER', 'BEFORE'];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild('nameInput') input: ElementRef;
  @ViewChild(NgxMaterialTimepickerComponent, {static: true}) public picker?: MatDatepicker<Date>;

  constructor(private taskService: TaskService, private taskCategoryService: TaskCategoriesService, private changeDetector: ChangeDetectorRef, private dialog: MatDialog, private toastr: ToastrService, @Inject(LOCALE_ID) private locale: string) {
  }

  ngOnInit(): void {
    this.dataSource = new TasksDataSource(this.taskService);
    this.taskService.get(this.defaultSize, this.defaultPage, this.defaultSort, this.deadlineMode, this.time).pipe(
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

  onSubmit() {
    this.isSubmitted = true;
  }

  loadTasksPage() {
    let categoryId: number = 0
    if (this.categoryName != null && this.categoryName != '') {
      this.taskCategoryService.get(1, 0, 'ASC', this.categoryName).subscribe(
        value => {
          if (value.elements != null && value.totalElements != 0) {
            categoryId = value.elements[0].id
          }

          this.load(this.paginator.pageSize,
            this.paginator.pageIndex,
            this.sort.direction,
            this.deadlineMode,
            this.time,
            this.taskName,
            categoryId,
            this.deadline)
        },
      )
    } else {
      this.load(this.paginator.pageSize,
        this.paginator.pageIndex,
        this.sort.direction,
        this.deadlineMode,
        this.time,
        this.taskName,
        undefined,
        this.deadline)
    }
  }

  load(pageSize: number, pageNumber: number, sortDirection: string, deadlineMode: string, time: string, name?: string, categoryId?: number, deadline?: Date) {
    this.dataSource.loadTasks(
      pageSize,
      pageNumber,
      sortDirection,
      deadlineMode,
      time,
      name,
      categoryId,
      deadline
    );
  }

  deleteTask(id: number) {
    this.taskService.delete(id).subscribe({
      next: () => {
        this.toastr.success("Successfully deleted task", '', {timeOut: 5000});
        setTimeout(() => {
          window.location.reload();
        }, 1800);
      },
      error: (error) => {
        setTimeout(() => {
          this.toastr.error(error.message, '', {timeOut: 3000})
        }, 500);
      }
    })
  }

  openAddTaskForm() {
    this.dialog.open(AddTaskComponent)
  }

  openEditTaskForm(data: Task) {
    this.dialog.open(EditTaskComponent, {data: data})
  }

  resetFilters() {
    window.location.reload()
  }
}
