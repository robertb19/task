import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, catchError, finalize, Observable, of} from "rxjs";
import {TaskCategoriesService} from "../../service/task-categories.service";
import {Page, TaskCategory} from "../../interface/task-category";

export class TaskCategoriesDataSource implements DataSource<TaskCategory> {
  totalElements : number = 5

  private taskCategorySubject = new BehaviorSubject<TaskCategory[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private taskCategoriesService: TaskCategoriesService) {
  }

  connect(collectionViewer: CollectionViewer): Observable<TaskCategory[]> {
    return this.taskCategorySubject.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.taskCategorySubject.complete();
    this.loadingSubject.complete();
  }

  loadTaskCategories(pageSize: number, pageNumber: number, sortDirection: string, name?: string) {
    this.loadingSubject.next(true);
    console.log('logging name here' + name)
    this.taskCategoriesService.get(pageSize, pageNumber, sortDirection.toUpperCase(), name).pipe(
      catchError(() => of([])),
      finalize(() => this.loadingSubject.next(false))
    )
      .subscribe(page => {
        let mappedPage = page as Page
        this.totalElements = mappedPage.totalElements
        this.taskCategorySubject.next(mappedPage.elements)
      });
  }
}
