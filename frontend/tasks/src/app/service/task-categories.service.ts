import { Injectable } from '@angular/core';
import {HttpHeaders, HttpClient, HttpParams} from '@angular/common/http';
import {catchError, map, Observable, of, throwError} from "rxjs";
import {globalProperties} from "../../properties";
import {AddTaskCategoryForm} from "../components/add-task-category/add-task-category.component";
import {Page} from "../interface/task-category";

var paths = {
  baseCategoryUrl: globalProperties.baseUrl + "/v1.0/categories",
  categoryWithIdUrl: globalProperties.baseUrl + "/v1.0/categories/",
}

@Injectable({
  providedIn: 'root'
})
export class TaskCategoriesService {

  constructor(private httpClient: HttpClient) { }

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
        catchError(() => throwError(() => new Error(`Unable to add task category`)))
      );
  }

  get(pageSize: number, pageNumber: number, sortDirection: string, name?: string): Observable<Page> {
    let params = new HttpParams();
    params= params.set('size', pageSize.toString())
    params = params.set('page', pageNumber.toString())
    params= params.set('sort', sortDirection)

    console.log(name + "and here ")
    if(name != null && name != '') {
      params = params.set('name',  name as string)
    }

    console.log("therse are the params " + params)
    return this.httpClient.get<Page>(
      paths.baseCategoryUrl, {
        params: params
      })
      .pipe(
        map(res => res),
        catchError(this.handleError<Page>('get',undefined))
      );
  }

/*  get(): Observable<Page> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json',
        'Cache-Control' : 'no-cache',
        'Pragma' : 'no-cache'
      }),
      observe: "response" as 'body'
    };
    return this.httpClient.get(//todo to change later to actual dynamic params switched from the html
      paths.baseCategoryUrl + "?page=0&size=100",
      httpOptions
    )
      .pipe(
        map((response: any) => response),
        catchError(() => throwError(() => new Error(`Unable to fetch task categories`)))
      );
  }*/

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error);
      return of(result as T);
    };
  }

}
