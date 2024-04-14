import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {catchError, map, Observable, of, throwError} from "rxjs";
import {globalProperties} from "../../properties";
import {AddTaskForm} from "../domain/task";
import {Page} from "../domain/generic";
import {EditTaskCategoryForm} from "../domain/task-category";

var paths = {
  baseTaskUrl: globalProperties.baseUrl + "/v1.0/tasks",
  baseTaskWithUrl: globalProperties.baseUrl + "/v1.0/tasks/",
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  constructor(private httpClient: HttpClient) { }

  addTask(task: AddTaskForm): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: "response" as 'body'
    };
    return this.httpClient.post(
      paths.baseTaskUrl,
      task,
      httpOptions)
      .pipe(
        map((response: any) => response),
        catchError(error => {
          if(error.error) {
            throw new Error(error.error.message)
          } else {
            throw new Error(`Unable to add task`)
          }
        })
      );
  }

  get(pageSize: number,
      pageNumber: number,
      sortDirection: string,
      deadlineMode : string,
      time : string,
      name?: string,
      categoryId? : number,
      deadline? : Date): Observable<Page> {
    let params = new HttpParams();
    params= params.set('size', pageSize.toString())
    params = params.set('page', pageNumber.toString())
    params= params.set('sort', sortDirection)

    if(deadlineMode == 'BEFORE') {
      params = params.set('deadlineMode', 'BEFORE')
    } else {
      params = params.set('deadlineMode', 'AFTER')
    }

    if(name != null && name != '') {
      params = params.set('name',  name as string)
    }

    if(categoryId != null && categoryId != 0) {
      params = params.set('category', categoryId as number)
    }

    if(deadline != null) {
      if(time != undefined && time != '') {
        deadline = this.getNewDeadline(deadline, time)
      }
      params = params.set('deadlineDate', deadline.getTime() / 1000) //as Epoch Seconds
    }

    return this.httpClient.get<Page>(
      paths.baseTaskUrl, {
        params: params
      })
      .pipe(
        map(res => res),
        catchError(this.handleError<Page>('get',undefined))
      );
  }

  delete(id: number) : Observable<any> {
    return this.httpClient.delete(paths.baseTaskWithUrl + id)
      .pipe(
        map((response: any) => response),
        catchError(() => throwError(() => new Error(`Unable to delete task`)))
      );
  }

  update(taskCategory: EditTaskCategoryForm, id : number): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: "response" as 'body'
    };
    return this.httpClient.patch(
      paths.baseTaskWithUrl + id,
      taskCategory,
      httpOptions)
      .pipe(
        map((response: any) => response),
        catchError(error => {
          if(error.error) {
            throw new Error(error.error.message)
          } else {
            throw new Error(`Unable to edit task`)
          }
        })
      );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error);
      return of(result as T);
    };
  }

  private getNewDeadline(date : Date, time: string) : Date {
    let deadlineCopy = new Date(date)
    let hoursMinutesAndTimeZone = time.toString().split(":")
    let minutesAndTimeOfDay = hoursMinutesAndTimeZone[1].split(" ")
    let hourAsNumber: number = +hoursMinutesAndTimeZone[0];
    let minuteAsNumber: number = +minutesAndTimeOfDay[0];
    if(minutesAndTimeOfDay[1] == 'PM') {
      if(hourAsNumber == 12) {
        deadlineCopy.setHours(12, minuteAsNumber)
      } else {
        deadlineCopy.setHours(hourAsNumber + 12, minuteAsNumber)
      }
    } else {
      if(hourAsNumber == 12) {
        deadlineCopy.setHours(0, minuteAsNumber)
      } else {
        deadlineCopy.setHours(hourAsNumber, minuteAsNumber)
      }
    }
    return deadlineCopy
  }
}
