import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, catchError, finalize, Observable, of} from "rxjs";
import {TaskCategory} from "../../domain/task-category";
import {Task} from "../../domain/task";
import {TaskService} from "../../service/task.service";
import {Page} from "../../domain/generic";

export class TasksDataSource implements DataSource<Task> {
  totalElements : number = 5

  private taskSubject = new BehaviorSubject<Task[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private taskService: TaskService) {
  }

  connect(collectionViewer: CollectionViewer): Observable<Task[]> {
    return this.taskSubject.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.taskSubject.complete();
    this.loadingSubject.complete();
  }

  loadTasks(pageSize: number, pageNumber: number, sortDirection: string, deadlineMode: string, name?: string, taskCategoryId?: number, deadline?: Date) {
    this.loadingSubject.next(true);
    console.log('logging name here' + name)
    this.taskService.get(pageSize, pageNumber, sortDirection.toUpperCase(), deadlineMode, name, taskCategoryId, deadline).pipe(
      catchError(() => of([])),
      finalize(() => this.loadingSubject.next(false))
    )
      .subscribe(page => {
        let mappedPage = page as Page
        this.totalElements = mappedPage.totalElements
        this.taskSubject.next(mappedPage.elements)
      });
  }
}
