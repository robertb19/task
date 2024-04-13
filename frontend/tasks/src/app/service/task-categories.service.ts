import {Injectable} from '@angular/core';
import {HttpHeaders, HttpClient, HttpParams} from '@angular/common/http';
import {catchError, map, Observable, of, throwError} from "rxjs";
import {globalProperties} from "../../properties";
import {AddTaskCategoryForm, EditTaskCategoryForm} from "../domain/task-category";
import {Page} from "../domain/generic";

var paths = {
  baseCategoryUrl: globalProperties.baseUrl + "/v1.0/categories",
  categoryWithIdUrl: globalProperties.baseUrl + "/v1.0/categories/",
}

@Injectable({
  providedIn: 'root'
})
export class TaskCategoriesService {

  constructor(private httpClient: HttpClient) {
  }

  addTaskCategory(taskCategory: AddTaskCategoryForm): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: "response" as 'body'
    };
    return this.httpClient.post(
      paths.baseCategoryUrl,
      taskCategory,
      httpOptions)
      .pipe(
        map((response: any) => response),
        catchError(error => {
          console.log(error.status + " this is the error status")
            if (error.status == 409) {
              throw new Error(`Task category with this name already exists`)
            } else {
              throw new Error(`Unable to add task category`)
            }
          }
        ));
  }

  get(pageSize: number, pageNumber: number, sortDirection: string, name?: string): Observable<Page> {
    let params = new HttpParams();
    params = params.set('size', pageSize.toString())
    params = params.set('page', pageNumber.toString())
    params = params.set('sort', sortDirection)

    console.log(name + "and here ")
    if (name != null && name != '') {
      params = params.set('name', name as string)
    }

    console.log("therse are the params " + params)
    return this.httpClient.get<Page>(
      paths.baseCategoryUrl, {
        params: params
      })
      .pipe(
        map(res => res),
        catchError(this.handleError<Page>('get', undefined))
      );
  }

  delete(id: number): Observable<any> {
    return this.httpClient.delete(paths.categoryWithIdUrl + id)
      .pipe(
        map((response: any) => response),
        catchError(() => throwError(() => new Error(`Unable to delete task category`)))
      );
  }

  update(taskCategory: EditTaskCategoryForm, id: number): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: "response" as 'body'
    };
    return this.httpClient.patch(
      paths.categoryWithIdUrl + id,
      taskCategory,
      httpOptions)
      .pipe(
        map((response: any) => response),
        catchError(() => throwError(() => new Error(`Unable to edit task category`)))
      );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.log(error +" heeeer eis the next error");
      return of(result as T);
    };
  }

}
