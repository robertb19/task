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
import {Page, TaskCategory} from "../../interface/task-category";
import {MatSort, MatSortModule, Sort} from '@angular/material/sort';
import {
  MatCell,
  MatColumnDef,
  MatHeaderCell,
  MatTable,
  MatTableDataSource,
  MatTableModule
} from '@angular/material/table';
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule} from "@angular/common/http";
import {MatInputModule} from "@angular/material/input";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {TaskCategoriesDataSource} from "./categories-data-source";
import {catchError, debounceTime, distinctUntilChanged, finalize, fromEvent, map, merge, of, tap} from "rxjs";
import {ActivatedRoute, RouterModule} from "@angular/router";
import {FormsModule} from "@angular/forms";

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
    FormsModule
  ],
  templateUrl: './view-task-categories.component.html',
  styleUrl: './view-task-categories.component.css'
})
export class ViewTaskCategoriesComponent implements AfterViewInit, OnInit {
  displayedColumns = ["id", "name", "description"];
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

  constructor(private taskCategoryService : TaskCategoriesService, private changeDetector: ChangeDetectorRef) {}

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

}
